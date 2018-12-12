package ca.pjer.spring.boot.jdbi;

import org.jdbi.v3.sqlobject.SqlOperation;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.Set;

public class AutoSqlObjectRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {

            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return beanDefinition.getMetadata().isIndependent()
                        && beanDefinition.getMetadata().isInterface()
                        && beanDefinition.getMetadata().hasAnnotatedMethods(SqlOperation.class.getName());
            }
        };
        scanner.addIncludeFilter(new AnnotationTypeFilter(AutoSqlObject.class));

        String basePackage = ClassUtils.getPackageName(importingClassMetadata.getClassName());

        Set<BeanDefinition> candidates = scanner.findCandidateComponents(basePackage);

        for (BeanDefinition candidate : candidates) {

            String candidateClassName = candidate.getBeanClassName();
            if (candidateClassName == null) {
                continue;
            }

            Class candidateClass = ClassUtils.resolveClassName(candidateClassName, ClassUtils.getDefaultClassLoader());

            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(SqlObjectFactoryBean.class);
            beanDefinition.getPropertyValues().add("jdbi", new RuntimeBeanReference("jdbi"));
            beanDefinition.getPropertyValues().add("type", candidateClass);

            registry.registerBeanDefinition(candidateClassName, beanDefinition);
        }
    }
}
