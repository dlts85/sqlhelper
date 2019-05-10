/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the LGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at  http://www.gnu.org/licenses/lgpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fjn.helper.sql.dialect;

import com.fjn.helper.sql.dialect.annotation.Driver;
import com.fjn.helper.sql.dialect.annotation.Name;
import com.fjn.helper.sql.dialect.internal.*;
import com.fjn.helper.sql.util.Holder;
import com.fjn.helper.sql.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.DatabaseMetaData;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DialectRegistry {
    private static final Logger logger;
    private static final Map<String, Dialect> nameToDialectMap;
    private static final Map<String, String> classNameToNameMap;
    private static final Map<DatabaseMetaData, Holder<Dialect>> dbToDialectMap;
    private static final Properties vendorDatabaseIdMappings = new Properties();

    public Dialect getDialectByClassName(final String className) {
        final String dialectName = (String) DialectRegistry.classNameToNameMap.get(className);
        if (dialectName != null) {
            return this.getDialectByName(dialectName);
        }

        return null;
    }

    public Dialect getDialectByName(final String databaseId) {

        return DialectRegistry.nameToDialectMap.get(databaseId);
    }

    public Dialect getDialectByDatabaseMetadata(final DatabaseMetaData databaseMetaData) {
        if (databaseMetaData != null) {
            return ((Holder<Dialect>) DialectRegistry.dbToDialectMap.get(databaseMetaData)).get();
        }
        return null;
    }

    private static void registerBuiltinDialects() {
        loadDatabaseIdMappings();

        final Class<? extends Dialect>[] dialects = (Class<? extends Dialect>[]) new Class[]{
                Cache71Dialect.class,
                CUBRIDDialect.class,
                DB2Dialect.class,
                DerbyDialect.class,
                FirebirdDialect.class,
                FrontBaseDialect.class,
                H2Dialect.class, HSQLDialect.class,
                InformixDialect.class,
                IngresDialect.class,
                InterbaseDialect.class,
                JDataStoreDialect.class,
                MckoiDialect.class,
                MimerSQLDialect.class,
                MySQLDialect.class,
                MariaDBDialect.class,
                OracleDialect.class,
                PointbaseDialect.class,
                PostgreSQLDialect.class,
                ProgressDialect.class,
                RDMSOS2200Dialect.class,
                SAPDBDialect.class,
                SQLServerDialect.class,
                SQLiteDialect.class,
                SybaseDialect.class,
                TeradataDialect.class,
                TimesTenDialect.class};
        Arrays.asList(dialects).forEach(DialectRegistry::registerDialectByClass);
    }

    private static void loadDatabaseIdMappings(){
        InputStream inputStream = DialectRegistry.class.getResourceAsStream("/sqlhelper-dialect-databaseid.properties");
        if(inputStream!=null){
            try {
                vendorDatabaseIdMappings.load(inputStream);
            }catch (Throwable ex){
                logger.error(ex.getMessage(), ex);
            }
            finally {
                try {
                    inputStream.close();
                }catch (Throwable ex){
                    // Ignore it
                }
            }
        }
    }

    public static Properties getVendorDatabaseIdMappings() {
        return vendorDatabaseIdMappings;
    }

    public static void setDatabaseId(String keywordsInDriver, String databaseId){
        vendorDatabaseIdMappings.setProperty(keywordsInDriver, databaseId);
    }

    public void registerDialectByClassName(final String className) throws ClassNotFoundException {
        this.registerDialect(null, className);
    }

    public void registerDialect(final String dialectName, final String className) throws ClassNotFoundException {
        final Class<? extends Dialect> clazz = loadDialectClass(className);
        try {
            final Dialect dialect = registerDialectByClass(clazz);
            if (!StringUtil.isBlank(dialectName) && dialect != null) {
                DialectRegistry.nameToDialectMap.put(dialectName, dialect);
            }
        } catch (Throwable ex) {
            DialectRegistry.logger.info(ex.getMessage(), ex);
        }
    }

    private static Class<? extends Dialect> loadDialectClass(final String className) throws ClassNotFoundException {
        return (Class<? extends Dialect>) loadImplClass(className, Dialect.class);
    }

    private static Class<? extends java.sql.Driver> loadDriverClass(final String className) throws ClassNotFoundException {
        return (Class<? extends java.sql.Driver>) loadImplClass(className, Driver.class);
    }

    private static Class loadImplClass(final String className, final Class superClass) throws ClassNotFoundException {
        Class clazz = null;
        try {
            clazz = Class.forName(className, true, DialectRegistry.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
        }
        if (clazz == null) {
            clazz = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
        }
        if (superClass.isAssignableFrom(clazz)) {
            return clazz;
        }
        final String error = "Class " + clazz.getCanonicalName() + " is not cast to " + superClass.getCanonicalName();
        throw new ClassCastException(error);
    }

    private static Dialect registerDialectByClass(final Class<? extends Dialect> clazz) {
        Dialect dialect = null;
        final Name nameAnno = (Name) clazz.getDeclaredAnnotation(Name.class);
        String name;
        if (nameAnno != null) {
            name = nameAnno.value();
            if (StringUtil.isBlank(name)) {
                throw new RuntimeException("@Name is empty in class" + clazz.getClass());
            }
        } else {
            final String simpleClassName = clazz.getSimpleName().toLowerCase();
            name = simpleClassName.replaceAll("dialect", "");
        }
        final Driver driverAnno = (Driver) clazz.getDeclaredAnnotation(Driver.class);
        Class<? extends java.sql.Driver> driverClass = null;
        Constructor<? extends Dialect> driverConstructor = null;
        if (driverAnno != null) {
            final String driverClassName = driverAnno.value();
            if (StringUtil.isBlank(driverClassName)) {
                throw new RuntimeException("@Driver is empty in class" + clazz.getClass());
            }
            try {
                driverClass = loadDriverClass(driverClassName);
                try {
                    driverConstructor = clazz.getDeclaredConstructor(java.sql.Driver.class);
                } catch (Throwable ex) {
                    DialectRegistry.logger.info("Can't find the driver based constructor for dialect {}", (Object) name);
                }
            } catch (Throwable ex) {
                DialectRegistry.logger.info("Can't find driver class {} for {}  dialect", (Object) driverClassName, (Object) name);
            }
        }
        if (driverClass == null || driverConstructor == null) {
            try {
                dialect = (Dialect) clazz.newInstance();
            } catch (InstantiationException e2) {
                final String error = "Class " + clazz.getCanonicalName() + "need a <init>() ";
                throw new ClassFormatError(error);
            } catch (IllegalAccessException e3) {
                final String error = "Class " + clazz.getCanonicalName() + "need a public <init>() ";
                throw new ClassFormatError(error);
            }
        } else {
            try {
                dialect = (AbstractDialect) driverConstructor.newInstance(driverClass);
            } catch (InstantiationException e2) {
                final String error = "Class " + clazz.getCanonicalName() + "need a <init>(Driver) ";
                throw new ClassFormatError(error);
            } catch (IllegalAccessException e3) {
                final String error = "Class " + clazz.getCanonicalName() + "need a public <init>(Driver) ";
                throw new ClassFormatError(error);
            } catch (InvocationTargetException e) {
                DialectRegistry.logger.error("Register dialect {} fail: {}", new Object[]{name, e.getMessage(), e});
            }
        }
        DialectRegistry.nameToDialectMap.put(name, dialect);
        DialectRegistry.classNameToNameMap.put(clazz.getCanonicalName(), name);
        setDatabaseId(name, name);
        return dialect;
    }

    static {
        logger = LoggerFactory.getLogger((Class) DialectRegistry.class);
        nameToDialectMap = new HashMap<String, Dialect>();
        classNameToNameMap = new HashMap<String, String>();
        dbToDialectMap = new HashMap<DatabaseMetaData, Holder<Dialect>>();
        registerBuiltinDialects();
    }
}