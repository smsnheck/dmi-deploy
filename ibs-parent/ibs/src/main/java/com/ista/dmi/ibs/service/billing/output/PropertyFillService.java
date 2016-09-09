package com.ista.dmi.ibs.service.billing.output;

import com.ista.dmi.common.domain.TimeRange;
import com.ista.dmi.common.domain.Uuid;
import com.ista.dmi.common.domain.value.Commissioning;
import com.ista.dmi.common.domain.value.ConsumptionAnalysisType;
import com.ista.dmi.common.domain.value.CurrencyValueType;
import com.ista.dmi.common.domain.value.NetGrossType;
import com.ista.dmi.ibs.business.preparation.BillingTreeData;
import com.ista.dmi.ibs.domain.BillingProperty;
import com.ista.dmi.ibs.domain.billing.output.BillingParameter;
import com.ista.dmi.ibs.domain.billing.output.Property;
import com.ista.dmi.pds.buildingstructure.PropertyRO;
import com.ista.dmi.pds.productstructure.BillingMpcRO;
import com.ista.dmi.pds.productstructure.ConsumptionAnalysisPcRO;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

/**
 * Service that fills a property and its child elements.
 */
public class PropertyFillService {

  @Inject
  private PropertyInvoiceBalanceFillService propertyInvoiceBalanceFillService;

  @Inject
  private BuildingFillService buildingFillService;

  @Inject
  private AddressFillService addressFillService;

  @Inject
  private CustomerFillService customerFillService;

  /**
   * Creates and fills the property domain model.
   * @param propertyRO propertyRO
   * @param billingTreeData billingTreeData
   * @return Property
   */
  public Property create(PropertyRO propertyRO, BillingTreeData billingTreeData) {
    Property property = new Property();
    // Fill base data
    property.setPropertyUuid(new Uuid(propertyRO.getUuid()));
    property.setCustomer(customerFillService.createCustomer(billingTreeData.getCustomerRO()));
    property.setPropertyNumber(propertyRO.getPropertyNumber());
    if (propertyRO.getAddress() != null) {
      property.setPropertyAddress(addressFillService.createPropertyAddress(propertyRO));
    }

    BillingParameter billingParameter = new BillingParameter();
    billingParameter.setBillingDate(new DateTime());
    BillingMpcRO billingMainProductConfigurationRO = billingTreeData.getBillingMainProductConfigurationRO();
    billingParameter.setBillingMainProductConfigurationUuid(new Uuid(billingMainProductConfigurationRO.getUuid()));
    billingParameter.setPeriod(new TimeRange(billingMainProductConfigurationRO.getInvoicingPeriod().getStartDate(), billingMainProductConfigurationRO.getInvoicingPeriod().getEndDate()));
    //TODO read from billingMainConfigurationRO if given.
    billingParameter.setCurrencyValueType(new CurrencyValueType(Currency.getInstance("EUR"), NetGrossType.GROSS));
    property.setBillingParameter(billingParameter);
    if (CollectionUtils.isNotEmpty(billingTreeData.getPreviousPeriodsBillingMainProductConfigurationUuids())) {
      property.setPreviousBillingMainProductConfigurationUuid(billingTreeData.getPreviousPeriodsBillingMainProductConfigurationUuids().get(0));
    }

    // Create buildings
    property.setBuildings(buildingFillService.createBuildings(billingTreeData));

    // Create property invoice balance
    property.setPropertyInvoiceBalance(propertyInvoiceBalanceFillService.createPropertyInvoiceBalance(billingTreeData));

    createCustomerSpecificBillingTexts(billingTreeData, property);
    property.setContactDetails(addressFillService.createContactDetails(billingTreeData.getBranchAddress()));

    final ConsumptionAnalysisPcRO consumptionAnalysisProductConfigurationRO = billingTreeData.getConsumptionAnalysisProductConfigurationRO();
    if (consumptionAnalysisProductConfigurationRO != null) {
      property.setConsumptionAnalysisType(consumptionAnalysisProductConfigurationRO.getConsumptionAnalysisType());
      property.setConsumptionAnalysisViolations(consumptionAnalysisProductConfigurationRO.getConsumptionAnalysisViolations());
      property.setConsumptionAnalysisForcedByIsta(Commissioning.ISTA.equals(consumptionAnalysisProductConfigurationRO.getCommissioning()));
      property.setPrintConsumptionAnalysisAdvertisingText(!Boolean.TRUE.equals(consumptionAnalysisProductConfigurationRO.getFeesChargeable())
        && CollectionUtils.isEmpty(property.getConsumptionAnalysisViolations()));
    } else {
      property.setConsumptionAnalysisType(ConsumptionAnalysisType.NONE);
      property.setConsumptionAnalysisForcedByIsta(false);
      property.setPrintConsumptionAnalysisAdvertisingText(false);
    }
    return property;
  }

  private void createCustomerSpecificBillingTexts(BillingTreeData billingTreeData, Property property) {
    if (billingTreeData.getCustomerSpecificBillingTextROs() != null) {
      List<String> customerSpecificBillingText = new ArrayList<>();

      billingTreeData.getCustomerSpecificBillingTextROs()
        .forEach(
          billingText -> {
            String[] billingsTexts = billingText.getMessage().split("\n");
            Collections.addAll(customerSpecificBillingText, billingsTexts);
          }
      );
      property.setCustomerSpecificBillingText(customerSpecificBillingText);
    }
  }

  /**
   * Fills the property domain model with calculated values.
   * @param property property to fill
   * @param billingProperty billingProperty
   */
  public void fill(final Property property, BillingProperty billingProperty) {
    // Fill buildings
    buildingFillService.fillBuildings(property.getBuildings(), billingProperty);
    // Fill property costs
    propertyInvoiceBalanceFillService.fill(property, billingProperty);
    propertyInvoiceBalanceFillService.fillTotalBalance(property.getPropertyInvoiceBalance(), property.getBuildings());
  }
}
