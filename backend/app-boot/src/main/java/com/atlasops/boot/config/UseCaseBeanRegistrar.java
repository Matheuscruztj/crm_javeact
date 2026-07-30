package com.atlasops.boot.config;

import java.beans.Introspector;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

/**
 * Registers application use cases as constructor-autowired Spring beans.
 *
 * <p>This keeps the app runnable without adding component annotations to every use case class.
 * Existing explicit bean definitions keep precedence and are not duplicated.
 */
@Configuration
public class UseCaseBeanRegistrar implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

  private static final String BASE_PACKAGE = "com.atlasops";

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
      throws BeansException {
    var scanner = new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(useCaseTypeFilter());

    for (BeanDefinition candidate : scanner.findCandidateComponents(BASE_PACKAGE)) {
      String className = candidate.getBeanClassName();
      if (className == null || className.isBlank()) {
        continue;
      }

      String beanName = Introspector.decapitalize(shortName(className));
      if (registry.containsBeanDefinition(beanName)) {
        continue;
      }

      GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
      beanDefinition.setBeanClassName(className);
      beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
      registry.registerBeanDefinition(beanName, beanDefinition);
    }
  }

  @Override
  public void postProcessBeanFactory(
      org.springframework.beans.factory.config.ConfigurableListableBeanFactory beanFactory)
      throws BeansException {
    // no-op
  }

  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE;
  }

  private static TypeFilter useCaseTypeFilter() {
    return (metadataReader, metadataReaderFactory) ->
        metadataReader.getClassMetadata().getClassName().endsWith("UseCase");
  }

  private static String shortName(String className) {
    int lastDot = className.lastIndexOf('.');
    return lastDot >= 0 ? className.substring(lastDot + 1) : className;
  }
}
