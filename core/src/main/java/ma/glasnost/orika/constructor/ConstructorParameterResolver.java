package ma.glasnost.orika.constructor;

import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.AnnotationParanamer;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;
import ma.glasnost.orika.metadata.ConstructorParameter;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ConstructorParameterResolver is a utility for resolving constructor
 * parameters as Property instances.
 * 
 * @author mattdeboer
 * 
 */
public class ConstructorParameterResolver {
    
    private final Paranamer paranamer = new CachingParanamer(new AdaptiveParanamer(new BytecodeReadingParanamer(),
            new AnnotationParanamer()));
    
    private final Map<java.lang.reflect.Type, Map<String, Set<Property>>> constructorPropertiesByType = new ConcurrentHashMap<>();
    
    /**
     * Resolves constructor arguments as properties for the specified type.
     * 
     * 
     * TODO: this isn't a correct mapping, as
     * 
     * @param type
     * @return the property (of any format)
     */
    public Map<String, Set<Property>> getProperties(java.lang.reflect.Type type) {
        
        Map<String, Set<Property>> properties = constructorPropertiesByType.get(type);
        if (properties == null) {
            
            Type<?> resolvedType = TypeFactory.valueOf(type);
            if (resolvedType.isConcrete() && !resolvedType.isImmutable()) {
                synchronized (resolvedType) {
                    Map<Constructor<?>, List<Property>> constructors = new ConcurrentHashMap<>();
                    Map<Property, Map<Constructor<?>, Integer>> constructorsByProperty = new ConcurrentHashMap<>();
                    
                    for (Constructor<?> constructor : resolvedType.getRawType().getConstructors()) {
                        
                        String[] names = paranamer.lookupParameterNames(constructor, false);
                        java.lang.reflect.Type[] types = constructor.getGenericParameterTypes();
                        List<Property> constructorArgs = new ArrayList<>();
                        
                        for (int i = 0; i < names.length; ++i) {
                            
                            Type<?> propertyType;
                            if (types[i] instanceof ParameterizedType) {
                                propertyType = TypeFactory.resolveValueOf((ParameterizedType) types[i], resolvedType);
                            } else {
                                propertyType = TypeFactory.resolveValueOf((Class<?>) types[i], resolvedType);
                            }
                            Property constructorArg = new ConstructorParameter(names[i], propertyType, null);

                            Map<Constructor<?>, Integer> associatedConstructors = constructorsByProperty.computeIfAbsent(constructorArg, k -> new HashMap<>());

                            associatedConstructors.put(constructor, i);
                            constructorArgs.add(constructorArg);
                        }
                        constructors.put(constructor, constructorArgs);
                        
                    }
                    properties = new HashMap<>();
                    for (Entry<Property, Map<Constructor<?>, Integer>> entry : constructorsByProperty.entrySet()) {
                        String key = entry.getKey().getExpression();
                        Set<Property> propertiesByName = properties.computeIfAbsent(key, k -> new LinkedHashSet<>());

                        propertiesByName.add(new ConstructorParameter(entry.getKey().getName(), entry.getKey().getType(), entry.getValue()));
                    }
                    constructorPropertiesByType.put(type, properties);
                }
            }
        }
        return properties;
    }
    
    /**
     * Resolves one or more constructor parameters found in the constructor(s)
     * of the specified type which have the specified name.
     * 
     * @param type
     *            the type for which to resolve the property
     * @param name
     *            the name of the constructor parameter
     * @return the set of parameters resolved
     */
    public Set<Property> getPossibleConstructorParams(java.lang.reflect.Type type, String name) {
        
        Map<String, Set<Property>> propertiesByType = getProperties(type);
        if (propertiesByType != null) {
            return propertiesByType.get(name);
        } else {
            return null;
        }
    }
}
