package ca.pjer.spring.boot.jdbi.autoconfigure;

import ca.pjer.spring.boot.jdbi.AutoSqlObject;
import ca.pjer.spring.boot.jdbi.EnableAutoSqlObject;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.SqlOperation;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import javax.sql.DataSource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class JdbiAutoConfiguration {

    @Bean
    static Jdbi jdbi(DataSource dataSource) {
        return Jdbi.create(dataSource);
    }

    @Bean
    static BeanDefinitionRegistryPostProcessor autoSqlObjectPostProcessor(Jdbi jdbi) {
        
        return new BeanDefinitionRegistryPostProcessor() {

            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
            }

            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

                List<Class> enabledClasses = beanFactory.getBeansWithAnnotation(EnableAutoSqlObject.class)
                        .values().stream().map(Object::getClass).collect(Collectors.toList());

                if (enabledClasses.isEmpty()) {
                    return;
                }

                jdbi.installPlugin(new SqlObjectPlugin());

                ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {

                    @Override
                    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                        return beanDefinition.getMetadata().isInterface()
                                && beanDefinition.getMetadata().hasAnnotatedMethods(SqlOperation.class.getName());
                    }
                };
                scanner.addIncludeFilter(new AnnotationTypeFilter(AutoSqlObject.class));

                for (Class enabledClass : enabledClasses) {

                    Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(enabledClass.getPackageName());

                    if (beanDefinitions.isEmpty()) {
                        continue;
                    }

                    for (BeanDefinition beanDefinition : beanDefinitions) {

                        String beanClassName = beanDefinition.getBeanClassName();

                        Class beanClass;
                        try {
                            beanClass = enabledClass.getClassLoader().loadClass(beanClassName);
                        } catch (ClassNotFoundException e) {
                            throw new BeanCreationException(beanClassName, e.getMessage(), e);
                        }

                        //noinspection unchecked
                        Object bean = jdbi.onDemand(beanClass);

                        beanFactory.registerSingleton(beanClassName, bean);
                    }
                }
            }
        };
    }
}
