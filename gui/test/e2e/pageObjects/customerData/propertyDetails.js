'use strict';

var helper = require('./../../helpers');

module.exports = {
    get: function (propertyUuid, cUuid) {
        var url = '#/domain/properties/' + propertyUuid + '/overview?cUuid=' + cUuid;
        return browser.get(url);
    },

    notifications: helper.pageObjects.notifications,

    tabBar: function() {
      return $('.property-overview-nav-bar');
    },
    tabPropertyDetails: function() {
      return $('#property-tab-bar-details-data');
    },
    tabPropertyDocuments: function() {
      return $('#property-tab-bar-documents-data');
    },
    
    property: function () {
        return {
            number: function () {
                return $('#property-property-number').getText();
            },
            address: function () {
                return $('#property-address').getText();
            },
            addressAdditive: function () {
                return $('#property-address-additive').getText();
            },
            postOfficeBox: function () {
                return $('#property-post-office-box').getText();
            },
            postalCodeAndCity: function () {
                return $('#property-postal-code-and-city').getText();
            },
            country: function () {
                return $('#property-country').getText();
            },
            executingBranch: function () {
                return $('#property-executing-branch').getText();
            },
            executingTeam: function () {
                return $('#property-executing-team').getText();
            },
            readingRoute: function () {
                return $('#property-reading-route').getText();
            },
            customerCenterBranch: function () {
                return $('#property-customer-center-branch').getText();
            },
            customerCenterTeam: function () {
                return $('#property-customer-center-team').getText();
            },
            bankAccountIban: function () {
                return $('#property-bank-account-iban').getText();
            },
            deliveryLock: function () {
                return $('#property-delivery-lock');
            },
            contactLock: function () {
                return $('#property-contact-lock');
            },
            markedForDeletion: function () {
                return $('#property-deletion-flag');
            },
            markedForDeletionIcon: function () {
                return $('.deletion-icon-checked');
            },
            managedByIstaDataCollection: function () {
                return {
                    checked: function () {
                        return $('#property-managed-by-ista-data-collection .fa-check-square-o');
                    },
                    unchecked: function () {
                        return $('#property-managed-by-ista-data-collection .fa-square-o');
                    },
                    checkBox: function () {
                        return $('#checkbox-managed-by-ista-data-collection');
                    }
                };
            },
            telephoneAdvertisingOptIn: function () {
                return {
                    checked: function () {
                        return $('#telephone-advertising-opt-in .icon-checked');
                    },
                    unchecked: function () {
                        return $('#telephone-advertising-opt-in .icon-unchecked');
                    }
                };
            },
            contactPersonNameSalutation: function () {
                return $('#property-contact-person-salutaion').getText();
            },
            contactPersonNameOne: function () {
              return $('#property-contact-person-name-one').getText();
            },
            contactPersonNameTwo: function () {
                return $('#property-contact-person-name-two').getText();
            },
            contactPersonNameThree: function () {
                return $('#property-contact-person-name-three').getText();
            },
            contactPersonNameFour: function () {
                return $('#property-contact-person-name-four').getText();
            },
            contactPersonNamePhone: function () {
                return $('#property-contact-person-phone').getText();
            },
            contactPersonNameAdditionalPhone: function () {
                return $('#property-contact-person-additionalphone').getText();
            },
            contactPersonNameFax: function () {
                return $('#property-contact-person-fax').getText();
            },
            contactPersonNameEmail: function () {
                return $('#property-contact-person-email').getText();
            },
            editButton: function () {
                return $('#btn-edit-property');
            },
            saveButton: function () {
                return $('#save-button');
            },
            cancelButton: function () {
                return $('#cancel-button');
            }
        };
    },

    customer: function () {
        return {
            alternativeBillingRecipientNumberIcon: function () {
                return $('#property-alternative-billing-recipient-number-icon');
            },
            number: function () {
                return $('#property-customer-customer-number').getText();
            },
            salutation: function () {
                return $('#property-customer-salutation').getText();
            },
            name1: function () {
                return $('#property-customer-name1').getText();
            },
            name2: function () {
                return $('#property-customer-name2').getText();
            },
            name3: function () {
                return $('#property-customer-name3').getText();
            },
            addressStreetWithNumber: function () {
                return $('#property-customer-address-street').getText();
            },
            addressPostalCode: function () {
                return $('#property-customer-address-postal-code').getText();
            },
            addressCity: function () {
                return $('#property-customer-address-city').getText();
            },
            classificationCode: function () {
                return $('#property-customer-classification-code').getText();
            },
            type: function () {
                return $('#property-customer-type').getText();
            },
            postNumber: function () {
                return $('#property-customer-post-number').getText();
            },
            industryCode: function () {
                return $('#property-customer-industry-code').getText();
            },
            phoneNumberWithExtension: function () {
                return $('#property-customer-telephone-number').getText();
            },
            eMailAddress: function () {
                return $('#property-customer-email-address').getText();
            }
        };
    },
    
    customerGroup: function () {
        return {
            number: function () {
                return $('#customer-group-number').getText();
            },
            salutation: function () {
                return $('#customer-group-salutation').getText();
            },
            name1: function () {
                return $('#customer-group-name1').getText();
            },
            name2: function () {
                return $('#customer-group-name2').getText();
            },
            name3: function () {
                return $('#customer-group-name3').getText();
            },
            name4: function () {
                return $('#customer-group-name4').getText();
            },
            addressStreetWithNumber: function () {
                return $('#customer-group-address-street').getText();
            },
            addressPostalCode: function () {
                return $('#customer-group-address-postal-code').getText();
            },
            addressCity: function () {
                return $('#customer-group-address-city').getText();
            },
            salesOffice: function () {
                return $('#customer-group-sales-office').getText();
            },
            attendingTeam: function () {
                return $('#customer-group-attending-team').getText();
            },
            salesSupportTeam: function () {
                return $('#customer-group-sales-support-team').getText();
            }
        };
    },

    koliNuliPanel: function() {
        return {
            koliNuliReceiveDate: function() {
                return $('#koliNuliReceiveDateInput');
            },
            getErrorsForKoliNuliReceiveDate: function() {
                return $$('#errors-for-kolinuli-receive > div');
            },
            saveKoliNuliReceiveDateBtn: function() {
                return $('#saveKoliNuliReceiveDateBtn');
            },
            releaseKoliNuliBtn: function() {
                return $('#releaseKoliNuliBtn');
            },
            missingMainProductConfigurationForKoliNuli: function() {
                return $('#property-missingMainProductConfigurationForKoliNuli');
            },
            releaseKoliNuliLabel: function() {
                return $('.label-success');
            }
        };
    },
    
    documentsPanel: function() {
        return {
            btnCostUserListPdf: function() {
                return $('#cost-user-list-pdf a');
            },
            btnRunServiceInvoiceProcessStart: function() {
                return $('#run-service-invoice-process-start');
            },
            missingMainProductConfigurationForDcuments: function() {
                return $('#property-missingMainProductConfigurationForDocuments');
            }
        };
    },
    
    customerTooltip: function () {
        return $('.statistics-info .tooltip');
    },
    customerNumberToggleDetails: function () {
        return $('.statistics-info .customer-number');
    },
    customerNameToggleDetails: function () {
        return $('.statistics-info .customer-name');
    },
    customerIcon: function () {
        return $('.statistics-info .icon-user');
    }
};
     

