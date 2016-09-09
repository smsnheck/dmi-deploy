package com.ista.dmi.ibs.service.billing.output;

import com.ista.dmi.common.domain.Uuid;
import com.ista.dmi.common.domain.value.Commissioning;
import com.ista.dmi.common.domain.value.ConsumptionAnalysisType;
import com.ista.dmi.common.domain.value.ConsumptionAnalysisViolation;
import com.ista.dmi.common.domain.value.ServiceType;
import com.ista.dmi.common.resource.objects.AddressRO;
import com.ista.dmi.common.resource.objects.TimeRangeRO;
import com.ista.dmi.ibs.business.preparation.BillingTreeData;
import com.ista.dmi.ibs.domain.BillingFacility;
import com.ista.dmi.ibs.domain.BillingGroup;
import com.ista.dmi.ibs.domain.BillingProperty;
import com.ista.dmi.ibs.domain.billing.output.Building;
import com.ista.dmi.ibs.domain.billing.output.Property;
import com.ista.dmi.ibs.domain.billing.output.PropertyInvoiceBalance;
import com.ista.dmi.ibs.domain.billingstructure.EnergyServiceCostDistributionGroup;
import com.ista.dmi.pds.buildingstructure.PropertyRO;
import com.ista.dmi.pds.productstructure.BillingMpcRO;
import com.ista.dmi.pds.productstructure.ConsumptionAnalysisPcRO;
import com.ista.dmi.pds.productstructure.CustomerSpecificBillingTextRO;
import com.ista.dmi.soap.headerservice.TeamAddressType;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PropertyFillServiceTest {

  @Mock
  private BuildingFillService buildingFillServiceMock;

  @Mock
  private PropertyInvoiceBalanceFillService propertyInvoiceBalanceFillServiceMock;

  @Mock
  private AddressFillService addressFillServiceMock;

  @Mock
  private CustomerFillService customerFillServiceMock;

  @InjectMocks
  private PropertyFillService propertyFillService;

  private BillingTreeData billingTreeData;
  private PropertyRO propertyRO;
  private BillingProperty billingProperty;
  private BillingFacility billingFacility;
  private BillingGroup billingGroup;
  private Uuid propertyUuid;
  private Uuid billingMainProductConfigurationUuid;
  private TeamAddressType branchAddress;

  @Before
  public void setUpGivenData() {
    // Initialialize billing property data
    propertyUuid = Uuid.randomUuid();
    propertyRO = new PropertyRO();
    propertyRO.setUuid(propertyUuid.getUuid());
    propertyRO.setPropertyNumber("02-540-1200/9");
    AddressRO address = new AddressRO();
    propertyRO.setAddress(address);

    billingTreeData = new BillingTreeData();
    billingMainProductConfigurationUuid = Uuid.randomUuid();
    BillingMpcRO billingMainProductConfigurationRO = new BillingMpcRO();
    billingMainProductConfigurationRO.setUuid(billingMainProductConfigurationUuid.getUuid());
    billingTreeData.setBillingMainProductConfigurationRO(billingMainProductConfigurationRO);
    billingMainProductConfigurationRO.setInvoicingPeriod(new TimeRangeRO("2013-01-01", "2014-01-01"));
    CustomerSpecificBillingTextRO customerSpecificBillingTextRO = new CustomerSpecificBillingTextRO();
    customerSpecificBillingTextRO.setMessage("Sehr geehrter Herr Mustermann\nanbei erhalten sie Ihre Abrechnung.");
    billingTreeData.setCustomerSpecificBillingTextROs(Collections.singletonList(customerSpecificBillingTextRO));

    billingTreeData.setBranchAddress(branchAddress);

    billingProperty = new BillingProperty();
    billingFacility = new BillingFacility();
    billingGroup = new BillingGroup();
    billingGroup.setDistributionGroup(new EnergyServiceCostDistributionGroup());
    billingGroup.setServiceType(ServiceType.HEATING_ENERGY);
    billingFacility.addBillingGroup(billingGroup);
    billingProperty.addFacility(billingFacility);
  }

  @Test
  public void shouldCreateAndFillPropertyWithBaseData() {
    // given
    final Set<Building> buildingsMock = mock(Set.class);
    when(buildingFillServiceMock.createBuildings(billingTreeData)).thenReturn(buildingsMock);
    final PropertyInvoiceBalance propertyInvoiceBalanceMock = mock(PropertyInvoiceBalance.class);
    when(propertyInvoiceBalanceFillServiceMock.createPropertyInvoiceBalance(billingTreeData)).thenReturn(propertyInvoiceBalanceMock);
    com.ista.dmi.ibs.domain.billing.output.Address address = mock(com.ista.dmi.ibs.domain.billing.output.Address.class);
    when(addressFillServiceMock.createPropertyAddress(propertyRO)).thenReturn(address);

    ConsumptionAnalysisPcRO consumptionAnalysisPcRO = new ConsumptionAnalysisPcRO();
    consumptionAnalysisPcRO.setConsumptionAnalysisType(ConsumptionAnalysisType.COST_AND_CONSUMPTION_2_YEARS);
    consumptionAnalysisPcRO.setFeesChargeable(false);
    billingTreeData.setConsumptionAnalysisProductConfigurationRO(consumptionAnalysisPcRO);

    // when
    Property property = propertyFillService.create(propertyRO, billingTreeData);

    //Then
    assertThat(property, notNullValue());
    assertThat(property.getPropertyUuid(), equalTo(propertyUuid));
    assertThat(property.getPropertyNumber(), equalTo("02-540-1200/9"));

    assertThat(property.getPropertyAddress(), equalTo(address));
    assertThat(property.getBillingParameter(), notNullValue());

    assertThat(property.getBillingParameter().getPeriod().getStartDate(), equalTo(new DateTime(2013, 1, 1, 0, 0)));
    assertThat(property.getBillingParameter().getPeriod().getEndDate(), equalTo(new DateTime(2014, 1, 1, 0, 0)));
    assertThat(property.getBillingParameter().getBillingDate(), notNullValue());
    assertThat(property.getBillingParameter().getBillingMainProductConfigurationUuid(), equalTo(billingMainProductConfigurationUuid));
    assertThat(property.getBuildings(), equalTo(buildingsMock));
    assertThat(property.getPropertyInvoiceBalance(), equalTo(propertyInvoiceBalanceMock));
    assertThat(property.getCustomerSpecificBillingText(), notNullValue());
    assertThat(property.getCustomerSpecificBillingText().size(), equalTo(2));
    assertThat(property.getCustomerSpecificBillingText().get(0), equalTo("Sehr geehrter Herr Mustermann"));
    assertThat(property.getCustomerSpecificBillingText().get(1), equalTo("anbei erhalten sie Ihre Abrechnung."));
    assertThat(property.getConsumptionAnalysisType(), equalTo(consumptionAnalysisPcRO.getConsumptionAnalysisType()));
    assertThat(property.isConsumptionAnalysisForcedByIsta(), is(false));
    assertThat(property.isPrintConsumptionAnalysisAdvertisingText(), is(true));
    assertThat(property.getPreviousBillingMainProductConfigurationUuid(), nullValue());

    verify(addressFillServiceMock).createContactDetails(eq(branchAddress));
  }

  @Test
  public void shouldCreateAndFillPropertyWithoutConsumptionAnalysisData() {
    // when
    Property property = propertyFillService.create(propertyRO, billingTreeData);

    //then
    assertThat(property.getConsumptionAnalysisType(), equalTo(ConsumptionAnalysisType.NONE));
    assertThat(property.getConsumptionAnalysisViolations(), nullValue());
    assertThat(property.isConsumptionAnalysisForcedByIsta(), is(false));
    assertThat(property.isPrintConsumptionAnalysisAdvertisingText(), is(false));
  }

  @Test
  public void shouldCreateAndFillPropertyConsumptionAnalysisDataWithFeesCharged() {
    // when
    ConsumptionAnalysisPcRO consumptionAnalysisPcRO = new ConsumptionAnalysisPcRO();
    consumptionAnalysisPcRO.setConsumptionAnalysisType(ConsumptionAnalysisType.COST_AND_CONSUMPTION_2_YEARS);
    consumptionAnalysisPcRO.setFeesChargeable(true);
    billingTreeData.setConsumptionAnalysisProductConfigurationRO(consumptionAnalysisPcRO);

    Property property = propertyFillService.create(propertyRO, billingTreeData);

    //then
    assertThat(property.getConsumptionAnalysisType(), equalTo(ConsumptionAnalysisType.COST_AND_CONSUMPTION_2_YEARS));
    assertThat(property.getConsumptionAnalysisViolations(), nullValue());
    assertThat(property.isConsumptionAnalysisForcedByIsta(), is(false));
    assertThat(property.isPrintConsumptionAnalysisAdvertisingText(), is(false));
  }

  @Test
  public void shouldCreateAndFillPropertyConsumptionAnalysisDataWithForcedByIsta() {
    // when
    ConsumptionAnalysisPcRO consumptionAnalysisPcRO = new ConsumptionAnalysisPcRO();
    consumptionAnalysisPcRO.setConsumptionAnalysisType(ConsumptionAnalysisType.COST_3_YEARS);
    consumptionAnalysisPcRO.setCommissioning(Commissioning.ISTA);
    billingTreeData.setConsumptionAnalysisProductConfigurationRO(consumptionAnalysisPcRO);

    Property property = propertyFillService.create(propertyRO, billingTreeData);

    //then
    assertThat(property.getConsumptionAnalysisType(), equalTo(ConsumptionAnalysisType.COST_3_YEARS));
    assertThat(property.getConsumptionAnalysisViolations(), nullValue());
    assertThat(property.isPrintConsumptionAnalysisAdvertisingText(), is(true));
  }

  @Test
  public void shouldCreateAndFillPropertyConsumptionAnalysisDataWithViolations() {
    // when
    ConsumptionAnalysisPcRO consumptionAnalysisPcRO = new ConsumptionAnalysisPcRO();
    consumptionAnalysisPcRO.setConsumptionAnalysisType(ConsumptionAnalysisType.NONE);
    consumptionAnalysisPcRO.setConsumptionAnalysisViolations(Collections.singletonList(ConsumptionAnalysisViolation.NOT_CONSUMPTION_DEPENDENT));
    consumptionAnalysisPcRO.setFeesChargeable(false);
    billingTreeData.setConsumptionAnalysisProductConfigurationRO(consumptionAnalysisPcRO);

    Property property = propertyFillService.create(propertyRO, billingTreeData);

    //then
    assertThat(property.getConsumptionAnalysisType(), equalTo(ConsumptionAnalysisType.NONE));
    assertThat(property.getConsumptionAnalysisViolations(), contains(ConsumptionAnalysisViolation.NOT_CONSUMPTION_DEPENDENT));
    assertThat(property.isConsumptionAnalysisForcedByIsta(), is(false));
    assertThat(property.isPrintConsumptionAnalysisAdvertisingText(), is(false));
  }

  @Test
  public void shouldCreateAndFillPropertyWithPreviousBillingMainProductConfigurationUuid() {
    // when
    final Uuid previousBillingMPC = Uuid.randomUuid();
    billingTreeData.setPreviousPeriodsBillingMainProductConfigurationUuids(Arrays.asList(previousBillingMPC, Uuid.randomUuid()));
    Property property = propertyFillService.create(propertyRO, billingTreeData);

    //then
    assertThat(property.getPreviousBillingMainProductConfigurationUuid(), equalTo(previousBillingMPC));
  }

  @Test
  public void shouldCallPropertyInvoiceBalanceFillService() {
    //given
    Property property = new Property();
    property.setPropertyInvoiceBalance(new PropertyInvoiceBalance());
    Set<Building> buildings = Collections.singleton(new Building());
    property.setBuildings(buildings);

    //When
    propertyFillService.fill(property, billingProperty);

    //Then
    verify(buildingFillServiceMock).fillBuildings(property.getBuildings(), billingProperty);
    verify(propertyInvoiceBalanceFillServiceMock).fill(property, billingProperty);
    verify(propertyInvoiceBalanceFillServiceMock).fillTotalBalance(property.getPropertyInvoiceBalance(), property.getBuildings());
  }
}
