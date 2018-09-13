package ca.pjer.spring.boot.jdbi.autoconfigure;

import ca.pjer.spring.boot.jdbi.AutoSqlObject;
import ca.pjer.spring.boot.jdbi.SqlObjectFactoryBean;
import org.jdbi.v3.sqlobject.SqlOperation;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
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

            AbstractBeanDefinition beanDefinition =
                    BeanDefinitionBuilder.genericBeanDefinition(SqlObjectFactoryBean.class)
                            .addPropertyReference("jdbi", "jdbi")
                            .addPropertyValue("type", candidateClassName)
                            .getBeanDefinition();

            BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, candidateClassName);

            BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
        }
    }
}
