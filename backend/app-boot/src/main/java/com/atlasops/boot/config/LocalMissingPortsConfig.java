package com.atlasops.boot.config;

import com.atlasops.ai.domain.ports.PendingApprovalRepository;
import com.atlasops.approvals.domain.ports.ApprovalPort;
import com.atlasops.audit.domain.ports.LedgerRepository;
import com.atlasops.customers.domain.ports.GeospatialCustomerPort;
import com.atlasops.imports.domain.ports.ImportPort;
import com.atlasops.operations.domain.ports.OperationsPort;
import com.atlasops.operations.domain.ports.ProjectionRepository;
import java.beans.Introspector;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Registers explicit local-only no-op fallbacks for modules that still lack production adapters.
 */
@Configuration(proxyBeanMethods = false)
@Profile("local")
public final class LocalMissingPortsConfig {

  private static final Set<Class<?>> LOCAL_FALLBACK_PORTS =
      Set.of(
          ApprovalPort.class,
          GeospatialCustomerPort.class,
          ImportPort.class,
          LedgerRepository.class,
          OperationsPort.class,
          PendingApprovalRepository.class,
          ProjectionRepository.class);

  private LocalMissingPortsConfig() {}

  @Bean
  static BeanFactoryPostProcessor localMissingPortFallbacks() {
    return beanFactory -> {
      if (!(beanFactory instanceof DefaultListableBeanFactory registry)) {
        return;
      }

      for (Class<?> portType : LOCAL_FALLBACK_PORTS) {
        String[] beanNames =
            beanFactory.getBeanNamesForType(ResolvableType.forClass(portType), false, false);
        if (beanNames.length > 0) {
          continue;
        }

        String beanName = Introspector.decapitalize(portType.getSimpleName()) + "LocalFallback";
        registry.registerSingleton(beanName, createNoOpProxy(portType));
        LoggerFactory.getLogger(LocalMissingPortsConfig.class)
            .warn("Registered local fallback bean for missing port {}", portType.getName());
      }
    };
  }

  private static Object createNoOpProxy(Class<?> portType) {
    InvocationHandler handler =
        (proxy, method, args) -> {
          if (method.getDeclaringClass() == Object.class) {
            return handleObjectMethod(proxy, method, args, portType);
          }

          LoggerFactory.getLogger(portType)
              .warn("Local fallback invoked for {}.{}", portType.getSimpleName(), method.getName());
          return defaultValue(method);
        };

    return Proxy.newProxyInstance(portType.getClassLoader(), new Class<?>[] {portType}, handler);
  }

  private static Object handleObjectMethod(
      Object proxy, Method method, Object[] args, Class<?> portType) {
    return switch (method.getName()) {
      case "toString" -> "LocalFallbackProxy[" + portType.getName() + "]";
      case "hashCode" -> System.identityHashCode(proxy);
      case "equals" -> proxy == args[0];
      default -> null;
    };
  }

  private static Object defaultValue(Method method) {
    Class<?> returnType = method.getReturnType();

    if (returnType == Void.TYPE) {
      return null;
    }
    if (returnType == Boolean.TYPE || returnType == Boolean.class) {
      return false;
    }
    if (returnType == Integer.TYPE || returnType == Integer.class) {
      return 0;
    }
    if (returnType == Long.TYPE || returnType == Long.class) {
      return 0L;
    }
    if (returnType == Double.TYPE || returnType == Double.class) {
      return 0.0d;
    }
    if (returnType == Float.TYPE || returnType == Float.class) {
      return 0.0f;
    }
    if (returnType == Short.TYPE || returnType == Short.class) {
      return (short) 0;
    }
    if (returnType == Byte.TYPE || returnType == Byte.class) {
      return (byte) 0;
    }
    if (returnType == Character.TYPE || returnType == Character.class) {
      return '\0';
    }
    if (Optional.class.isAssignableFrom(returnType)) {
      return Optional.empty();
    }
    if (List.class.isAssignableFrom(returnType)) {
      return List.of();
    }
    if (Set.class.isAssignableFrom(returnType)) {
      return Set.of();
    }
    if (Map.class.isAssignableFrom(returnType)) {
      return Map.of();
    }
    if (Page.class.isAssignableFrom(returnType)) {
      return Page.empty(Pageable.unpaged());
    }
    return null;
  }
}
