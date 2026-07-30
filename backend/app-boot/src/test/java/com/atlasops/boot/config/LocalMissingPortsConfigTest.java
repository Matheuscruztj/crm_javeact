package com.atlasops.boot.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlasops.approvals.domain.ports.ApprovalPort;
import com.atlasops.customers.domain.ports.GeospatialCustomerPort;
import com.atlasops.operations.domain.ports.OperationsPort;
import com.atlasops.operations.domain.ports.ProjectionRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class LocalMissingPortsConfigTest {

  @Test
  void should_registerNoOpFallbacks_whenPortsAreMissing() {
    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

    LocalMissingPortsConfig.localMissingPortFallbacks().postProcessBeanFactory(beanFactory);

    ApprovalPort approvalPort = beanFactory.getBean(ApprovalPort.class);
    GeospatialCustomerPort geospatialCustomerPort = beanFactory.getBean(GeospatialCustomerPort.class);
    OperationsPort operationsPort = beanFactory.getBean(OperationsPort.class);
    ProjectionRepository projectionRepository = beanFactory.getBean(ProjectionRepository.class);

    assertThat(approvalPort.submitApproval(null)).isNull();

    Pageable pageable = PageRequest.of(0, 10);
    assertThat(geospatialCustomerPort.findWithinRadius(1.0, 2.0, 3.0, "tenant-1", pageable))
        .isNotNull()
        .isEmpty();

    assertThat(operationsPort.getSystemHealth()).isNull();
    assertThat(projectionRepository.findAll()).isEmpty();
    assertThat(projectionRepository.findByName("search-index")).isEmpty();

    assertThat(beanFactory.containsBean("approvalPortLocalFallback")).isTrue();
    assertThat(beanFactory.containsBean("operationsPortLocalFallback")).isTrue();
  }
}
