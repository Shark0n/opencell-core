/*
 * (C) Copyright 2015-2016 Opencell SAS (http://opencellsoft.com/) and contributors.
 * (C) Copyright 2009-2014 Manaty SARL (http://manaty.net/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.service.script;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ElementNotFoundException;
import org.meveo.admin.exception.InvalidPermissionException;
import org.meveo.admin.exception.InvalidScriptException;
import org.meveo.admin.util.ResourceBundle;
import org.meveo.commons.utils.FileUtils;
import org.meveo.commons.utils.StringUtils;
import org.meveo.event.monitoring.ClusterEventPublisher;
import org.meveo.event.monitoring.ClusterEventDto.CrudActionEnum;
import org.meveo.model.IEntity;
import org.meveo.model.scripts.CustomScript;
import org.meveo.model.scripts.ScriptInstanceError;
import org.meveo.model.scripts.ScriptSourceTypeEnum;
import org.meveo.service.base.BusinessService;

public abstract class CustomScriptService<T extends CustomScript, SI extends ScriptInterface> extends BusinessService<T> {

    @Inject
    private ResourceBundle resourceMessages;

    @Inject
    private ClusterEventPublisher clusterEventPublisher;

    protected final Class<SI> scriptInterfaceClass;

    private Map<String, List<String>> allLogs = new HashMap<String, List<String>>();

    private Map<String, Class<SI>> allScriptInterfaces = new HashMap<String, Class<SI>>();

    private Map<String, SI> allScriptInstances = new HashMap<String, SI>();

    private CharSequenceCompiler<SI> compiler;

    private String classpath = "";

    /**
     * Constructor.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public CustomScriptService() {
        super();
        Class clazz = getClass();
        while (!(clazz.getGenericSuperclass() instanceof ParameterizedType)) {
            clazz = clazz.getSuperclass();
        }
        Object o = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[1];

        if (o instanceof TypeVariable) {
            this.scriptInterfaceClass = (Class<SI>) ((TypeVariable) o).getBounds()[0];
        } else {
            this.scriptInterfaceClass = (Class<SI>) o;
        }
    }

    /**
     * Find scripts by source type
     * 
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<T> findByType(ScriptSourceTypeEnum type) {
        List<T> result = new ArrayList<T>();
        try {
            result = (List<T>) getEntityManager().createNamedQuery("CustomScript.getScriptInstanceByTypeActive").setParameter("sourceTypeEnum", type).getResultList();
        } catch (NoResultException e) {

        }
        return result;
    }

    @Override
    public void create(T script) throws BusinessException {

        String className = getClassName(script.getScript());
        if (className == null) {
            throw new BusinessException(resourceMessages.getString("message.scriptInstance.sourceInvalid"));
        }
        String fullClassName = getFullClassname(script.getScript());

        if (isOverwritesJavaClass(fullClassName)) {
            throw new BusinessException(resourceMessages.getString("message.scriptInstance.classInvalid", fullClassName));
        }
        script.setCode(fullClassName);

        super.create(script);
        compileScript(script, false);

        clusterEventPublisher.publishEvent(script, CrudActionEnum.create);
    }

    @Override
    public T update(T script) throws BusinessException {

        String className = getClassName(script.getScript());
        if (className == null) {
            throw new BusinessException(resourceMessages.getString("message.scriptInstance.sourceInvalid"));
        }

        String fullClassName = getFullClassname(script.getScript());
        if (isOverwritesJavaClass(fullClassName)) {
            throw new BusinessException(resourceMessages.getString("message.scriptInstance.classInvalid", fullClassName));
        }

        script.setCode(fullClassName);

        script = super.update(script);

        compileScript(script, false);

        clusterEventPublisher.publishEvent(script, CrudActionEnum.update);
        return script;
    }

    @Override
    public void remove(T script) throws BusinessException {
        super.remove(script);

        clusterEventPublisher.publishEvent(script, CrudActionEnum.remove);
    }

    @Override
    public T enable(T script) throws BusinessException {

        script = super.enable(script);

        clusterEventPublisher.publishEvent(script, CrudActionEnum.enable);

        return script;
    }

    @Override
    public T disable(T script) throws BusinessException {
        
        script = super.disable(script);

        clusterEventPublisher.publishEvent(script, CrudActionEnum.disable);

        return script;
    }

    /**
     * Check full class name is existed class path or not
     */
    public static boolean isOverwritesJavaClass(String fullClassName) {
        try {
            Class.forName(fullClassName);
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    /**
     * Construct classpath for script compilation
     * 
     * @throws IOException
     */
    private void constructClassPath() throws IOException {

        if (classpath.length() == 0) {
            String jbossHome = System.getProperty("jboss.home.dir");
            File deploymentLibDirs = new File(jbossHome + "/standalone/tmp/vfs/deployment");
            if (!deploymentLibDirs.exists()) {
                log.error("cannot find " + jbossHome + "/standalone/tmp/vfs/deployment .. are you deploying on jboss ?");
                return;
            } else {
                File deploymentDir = null;
                for (File deployment : deploymentLibDirs.listFiles()) {
                    if (deploymentDir == null) {
                        deploymentDir = deployment;
                    } else {
                        if (deployment.lastModified() > deploymentDir.lastModified()) {
                            deploymentDir = deployment;
                        }
                    }
                }
                for (File physicalLibDir : deploymentDir.listFiles()) {
                    if (physicalLibDir.isDirectory()) {
                        for (File f : FileUtils.getFilesToProcess(physicalLibDir, "*", "jar")) {
                            classpath += f.getCanonicalPath() + File.pathSeparator;
                        }
                    }
                }
            }
        }
        log.info("compileAll classpath={}", classpath);

    }

    /**
     * Build the classpath and compile all scripts
     */
    protected void compile(List<T> scripts) {
        try {

            constructClassPath();

            for (T script : scripts) {
                compileScript(script, false);
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /*
     * Compile a script
     */
    public void refreshCompiledScript(String scriptCode) {

        T script = findByCode(scriptCode);
        if (script == null) {
            clearCompiledScripts(scriptCode);
        } else {
            compileScript(script, false);
        }
//        detach(script);
    }

    /**
     * Compile script, a and update script entity status with compilation errors. Successfully compiled script is added to a compiled script cache if active and not in test
     * compilation mode.
     * 
     * @param script Script entity to compile
     * @param testCompile Is it a compilation for testing purpose. Won't clear nor overwrite existing compiled script cache.
     */
    public void compileScript(T script, boolean testCompile) {

        List<ScriptInstanceError> scriptErrors = compileScript(script.getCode(), script.getSourceTypeEnum(), script.getScript(), script.isActive(), testCompile);

        if (!testCompile) {
            script.setError(scriptErrors != null && !scriptErrors.isEmpty());
            script.setScriptErrors(scriptErrors);
        }
    }

    /**
     * Compile script. DOES NOT update script entity status. Successfully compiled script is added to a compiled script cache if active and not in test compilation mode.
     * 
     * @param scriptCode Script entity code
     * @param sourceType Source code language type
     * @param sourceCode Source code
     * @param isActive Is script active. It will compile it anyway. Will clear but not overwrite existing compiled script cache.
     * @param testCompile Is it a compilation for testing purpose. Won't clear nor overwrite existing compiled script cache.
     * 
     * @return A list of compilation errors if not compiled
     */
    private List<ScriptInstanceError> compileScript(String scriptCode, ScriptSourceTypeEnum sourceType, String sourceCode, boolean isActive, boolean testCompile) {

        log.debug("Compile script {}", scriptCode);

        try {
            if (!testCompile) {
                clearCompiledScripts(scriptCode);
            }

            // For now no need to check source type if (sourceType==ScriptSourceTypeEnum.JAVA){

            Class<SI> compiledScript = compileJavaSource(sourceCode);

            if (!testCompile && isActive) {

                allScriptInterfaces.put(scriptCode, compiledScript);
                allScriptInstances.put(scriptCode, compiledScript.newInstance());

                log.debug("Compiled script {} added to compiled interface map", scriptCode);
            }

            return null;

        } catch (CharSequenceCompilerException e) {
            log.error("Failed to compile script {}. Compilation errors:", scriptCode);

            List<ScriptInstanceError> scriptErrors = new ArrayList<>();

            List<Diagnostic<? extends JavaFileObject>> diagnosticList = e.getDiagnostics().getDiagnostics();
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnosticList) {
                if ("ERROR".equals(diagnostic.getKind().name())) {
                    ScriptInstanceError scriptInstanceError = new ScriptInstanceError();
                    scriptInstanceError.setMessage(diagnostic.getMessage(Locale.getDefault()));
                    scriptInstanceError.setLineNumber(diagnostic.getLineNumber());
                    scriptInstanceError.setColumnNumber(diagnostic.getColumnNumber());
                    scriptInstanceError.setSourceFile(diagnostic.getSource().toString());
                    // scriptInstanceError.setScript(scriptInstance);
                    scriptErrors.add(scriptInstanceError);
                    // scriptInstanceErrorService.create(scriptInstanceError, scriptInstance.getAuditable().getCreator());
                    log.warn("{} script {} location {}:{}: {}", diagnostic.getKind().name(), scriptCode, diagnostic.getLineNumber(), diagnostic.getColumnNumber(),
                        diagnostic.getMessage(Locale.getDefault()));
                }
            }
            return scriptErrors;

        } catch (Exception e) {
            log.error("Failed while compiling script", e);
            List<ScriptInstanceError> scriptErrors = new ArrayList<>();
            ScriptInstanceError scriptInstanceError = new ScriptInstanceError();
            scriptInstanceError.setMessage(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            scriptErrors.add(scriptInstanceError);

            return scriptErrors;
        }
    }

    /**
     * Compile java Source script
     * 
     * @param javaSrc Java source to compile
     * @return Compiled class instance
     * @throws CharSequenceCompilerException
     */
    protected Class<SI> compileJavaSource(String javaSrc) throws CharSequenceCompilerException {

        supplementClassPathWithMissingImports(javaSrc);

        String fullClassName = getFullClassname(javaSrc);

        log.trace("Compile JAVA script {} with classpath {}", fullClassName, classpath);

        compiler = new CharSequenceCompiler<SI>(this.getClass().getClassLoader(), Arrays.asList(new String[] { "-cp", classpath }));
        final DiagnosticCollector<JavaFileObject> errs = new DiagnosticCollector<JavaFileObject>();
        Class<SI> compiledScript = compiler.compile(fullClassName, javaSrc, errs, new Class<?>[] { scriptInterfaceClass });
        return compiledScript;
    }

    /**
     * Supplement classpath with classes needed for the particular script compilation. Solves issue when classes server as jboss modules are referenced in script. E.g.
     * prg.slf4j.Logger
     * 
     * @param javaSrc Java source to compile
     */
    @SuppressWarnings("rawtypes")
    private void supplementClassPathWithMissingImports(String javaSrc) {

        String regex = "import (.*?);";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(javaSrc);
        while (matcher.find()) {
            String className = matcher.group(1);
            try {
                if (!className.startsWith("java") && !className.startsWith("org.meveo")) {
                    Class clazz = Class.forName(className);
                    try {
                        String location = clazz.getProtectionDomain().getCodeSource().getLocation().getFile();
                        if (location.startsWith("file:")) {
                            location = location.substring(5);
                        }
                        if (location.endsWith("!/")) {
                            location = location.substring(0, location.length() - 2);
                        }

                        if (!classpath.contains(location)) {
                            classpath += File.pathSeparator + location;
                        }

                    } catch (Exception e) {
                        log.warn("Failed to find location for class {}", className);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to find location for class {}", className);
            }
        }

    }

    /**
     * Find the script class for a given script code
     * 
     * @param scriptCode Script code
     * @return Script interface Class
     * @throws InvalidScriptException Were not able to instantiate or compile a script
     * @throws ElementNotFoundException Script not found
     */
    public Class<SI> getScriptInterface(String scriptCode) throws ElementNotFoundException, InvalidScriptException {
        Class<SI> result = null;

        result = allScriptInterfaces.get(scriptCode);

        if (result == null) {
            T script = findByCode(scriptCode);
            if (script == null) {
                log.debug("ScriptInstance with {} does not exist", scriptCode);
                throw new ElementNotFoundException(scriptCode, getEntityClass().getName());
            }
            compileScript(script, false);
            if (script.isError()) {
                log.debug("ScriptInstance {} failed to compile. Errors: {}", scriptCode, script.getScriptErrors());
                throw new InvalidScriptException(scriptCode, getEntityClass().getName());
            }
            result = allScriptInterfaces.get(scriptCode);
        }

        if (result == null) {
            log.debug("ScriptInstance with {} does not exist", scriptCode);
            throw new ElementNotFoundException(scriptCode, getEntityClass().getName());
        }

        log.debug("getScriptInterface scriptCode:{} -> {}", scriptCode, result);
        return result;
    }

    /**
     * Get a compiled script class
     * 
     * @param scriptCode Script code
     * @return A compiled script class
     * @throws InvalidScriptException Were not able to instantiate or compile a script
     * @throws ElementNotFoundException Script not found
     */
    public SI getScriptInstance(String scriptCode) throws ElementNotFoundException, InvalidScriptException {
        Class<SI> scriptClass = getScriptInterface(scriptCode);

        try {
            SI script = scriptClass.newInstance();
            return script;

        } catch (InstantiationException | IllegalAccessException e) {
            log.error("Failed to instantiate script {}", scriptCode, e);
            throw new InvalidScriptException(scriptCode, getEntityClass().getName());
        }
    }

    public SI getCachedScriptInstance(String scriptCode) throws ElementNotFoundException, InvalidScriptException {
        SI script = null;
        script = allScriptInstances.get(scriptCode);
        return script;
    }

    /**
     * Add a log line for a script
     * 
     * @param message
     * @param scriptCode
     */
    public void addLog(String message, String scriptCode) {

        if (!allLogs.containsKey(scriptCode)) {
            allLogs.put(scriptCode, new ArrayList<String>());
        }
        allLogs.get(scriptCode).add(message);
    }

    /**
     * Get logs for script
     * 
     * @param scriptCode
     * @return
     */
    public List<String> getLogs(String scriptCode) {

        if (!allLogs.containsKey(scriptCode)) {
            return new ArrayList<String>();
        }
        return allLogs.get(scriptCode);
    }

    /**
     * Clear all logs for a script
     * 
     * @param scriptCode
     */
    public void clearLogs(String scriptCode) {
        if (allLogs.containsKey(scriptCode)) {
            allLogs.get(scriptCode).clear();
        }
    }

    /**
     * Find the package name in a source java text
     * 
     * @param src Java source code
     * @return Package name
     */
    public static String getPackageName(String src) {
        return StringUtils.patternMacher("package (.*?);", src);
    }

    /**
     * Find the class name in a source java text
     * 
     * @param src Java source code
     * @return Class name
     */
    public static String getClassName(String src) {
        String className = StringUtils.patternMacher("public class (.*) extends", src);
        if (className == null) {
            className = StringUtils.patternMacher("public class (.*) implements", src);
        }
        return className != null ? className.trim() : null;
    }

    /**
     * Gets a full classname of a script by combining a package (if applicable) and a classname
     * 
     * @param script Java source code
     * @return Full classname
     */
    public static String getFullClassname(String script) {
        String packageName = getPackageName(script);
        String className = getClassName(script);
        return (packageName != null ? packageName.trim() + "." : "") + className;
    }

    /**
     * Execute action on an entity
     * 
     * @param entity Entity to execute action on
     * @param scriptCode Script to execute, identified by a code
     * @param encodedParameters Additional parameters encoded in URL like style param=value&param=value
     * @return Context parameters. Will not be null even if "context" parameter is null.
     * @throws InvalidPermissionException Insufficient access to run the script
     * @throws ElementNotFoundException Script not found
     * @throws BusinessException Any execution exception
     */
    public Map<String, Object> execute(IEntity entity, String scriptCode, String encodedParameters) throws InvalidPermissionException, ElementNotFoundException, BusinessException {

        return execute(entity, scriptCode, CustomScriptService.parseParameters(encodedParameters));
    }

    /**
     * Execute action on an entity
     * 
     * @param entity Entity to execute action on
     * @param scriptCode Script to execute, identified by a code
     * @param context Additional parameters
     * @return Context parameters. Will not be null even if "context" parameter is null.
     * @throws InvalidScriptException Were not able to instantiate or compile a script
     * @throws ElementNotFoundException Script not found
     * @throws InvalidPermissionException Insufficient access to run the script
     * @throws BusinessException Any execution exception
     */
    public Map<String, Object> execute(IEntity entity, String scriptCode, Map<String, Object> context)
            throws InvalidScriptException, ElementNotFoundException, InvalidPermissionException, BusinessException {

        if (context == null) {
            context = new HashMap<String, Object>();
        }
        context.put(Script.CONTEXT_ENTITY, entity);
        Map<String, Object> result = execute(scriptCode, context);
        return result;
    }

    /**
     * Execute action on an entity
     * 
     * @param entity Entity to execute action on
     * @param scriptCode Script to execute, identified by a code
     * @param context Method context
     * 
     * @return Context parameters. Will not be null even if "context" parameter is null.
     * @throws InvalidScriptException Were not able to instantiate or compile a script
     * @throws ElementNotFoundException Script not found
     * @throws InvalidPermissionException Insufficient access to run the script
     * @throws BusinessException Any execution exception
     */
    public Map<String, Object> execute(String scriptCode, Map<String, Object> context)
            throws ElementNotFoundException, InvalidScriptException, InvalidPermissionException, BusinessException {

        log.trace("Script {} to be executed with parameters {}", scriptCode, context);

        SI classInstance = getScriptInstance(scriptCode);
        classInstance.execute(context);

        log.trace("Script {} executed with parameters {}", scriptCode, context);
        return context;
    }

    /**
     * Execute a class that extends Script
     * 
     * @param compiledScript Compiled script class
     * @param context Method context
     * 
     * @return Context parameters. Will not be null even if "context" parameter is null.
     * @throws BusinessException Any execution exception
     */
    protected Map<String, Object> execute(SI compiledScript, Map<String, Object> context) throws BusinessException {
        if (context == null) {
            context = new HashMap<String, Object>();
        }

        compiledScript.execute(context);
        return context;
    }

    /**
     * Parse parameters encoded in URL like style param=value&param=value
     * 
     * @param encodedParameters Parameters encoded in URL like style param=value&param=value
     * @return A map of parameter keys and values
     */
    public static Map<String, Object> parseParameters(String encodedParameters) {
        Map<String, Object> parameters = new HashMap<String, Object>();

        if (!StringUtils.isBlank(encodedParameters)) {
            StringTokenizer tokenizer = new StringTokenizer(encodedParameters, "&");
            while (tokenizer.hasMoreElements()) {
                String paramValue = tokenizer.nextToken();
                String[] paramValueSplit = paramValue.split("=");
                if (paramValueSplit.length == 2) {
                    parameters.put(paramValueSplit[0], paramValueSplit[1]);
                } else {
                    parameters.put(paramValueSplit[0], null);
                }
            }

        }
        return parameters;
    }

    /**
     * Get all script interfaces *
     * 
     * @return the allScriptInterfaces
     */
    public Map<String, Class<SI>> getAllScriptInterfaces() {
        return allScriptInterfaces;
    }

    private void clearCompiledScripts(String scriptCode) {
        allScriptInstances.remove(scriptCode);
        allScriptInterfaces.remove(scriptCode);
        allLogs.remove(scriptCode);
    }
}