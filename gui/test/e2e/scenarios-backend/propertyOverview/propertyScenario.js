'use strict';

var helper = require('./../../helpers');
var flux = helper.uuid.fluxplatz;
var andromeda = helper.uuid.andromeda;
var propertyDetailsPage = require(__dirname + '/../../pageObjects/customerData/propertyDetails.js');

describe('Property overview', function () {
  beforeEach(function () {
    helper.login();
    helper.setSize();
  });

  describe('after login', function () {
    it('should show the property header', function () {
      browser.get('#/domain/properties/C3C54C52C53D41FA999BD6C08B18D061/overview');
      browser.waitForAngular();
      expect(browser.getCurrentUrl()).toMatch(/.*#\/domain\/properties\/C3C54C52C53D41FA999BD6C08B18D061\/overview.*$/);
      $('#property-header').isDisplayed();
      expect($('#property-header strong.property-number').getText()).toEqual('99-434-8265/6');
      expect($('#property-header .customer-number').getText()).toEqual('0009999999');
    });

    helper.testHomepageShortcut();
    helper.testPropertySearchShortcut();
  });
  
  describe('Handle flag for ista data collection', function() {

    it('should show enabled flag', function() {
      propertyDetailsPage.get(flux.it, flux.contInf_010114_090914);
      expect(propertyDetailsPage.property().managedByIstaDataCollection().checked().isDisplayed()).toBe(true);
    });

    it('should disable flag and save property', function() {
      propertyDetailsPage.property().editButton().click();
      expect(propertyDetailsPage.property().managedByIstaDataCollection().checkBox().isDisplayed()).toBe(true);
      expect(propertyDetailsPage.property().managedByIstaDataCollection().checkBox().isSelected()).toBe(true);
      // click three times on checkbox to ensure the ngDisabled check was only executed initial
      propertyDetailsPage.property().managedByIstaDataCollection().checkBox().click();
      expect(propertyDetailsPage.property().managedByIstaDataCollection().checkBox().isSelected()).toBe(false);
      propertyDetailsPage.property().managedByIstaDataCollection().checkBox().click();
      expect(propertyDetailsPage.property().managedByIstaDataCollection().checkBox().isSelected()).toBe(true);
      propertyDetailsPage.property().managedByIstaDataCollection().checkBox().click();
      propertyDetailsPage.property().saveButton().click();
      expect(propertyDetailsPage.property().managedByIstaDataCollection().unchecked().isDisplayed()).toBe(true);
    });

    it('should disable checkbox when initial state of managedByIstaDataCollection is false', function() {
      propertyDetailsPage.property().editButton().click();
      expect(propertyDetailsPage.property().managedByIstaDataCollection().checkBox().isSelected()).toBe(false);
      propertyDetailsPage.property().cancelButton().click();
    });
  
    it('should not add mark for customer address if billing address does not exist', function() {
      expect(propertyDetailsPage.customer().alternativeBillingRecipientNumberIcon().isPresent()).toBeFalsy();
    });
    
    it('should add mark for customer address if billing address exist', function() {
      propertyDetailsPage.get('C3C54C52C53D41FA999BD6C08B18D061', '0709BDB7039B4D01B85EB700C98292EE');
      browser.waitForAngular();
      expect(propertyDetailsPage.customer().alternativeBillingRecipientNumberIcon().isPresent()).toBeTruthy();
    });
  });
  
  describe('Handle property overview page', function () {
    
    it('should display a tab bar', function() {
      browser.get('#/domain/properties/C3C54C52C53D41FA999BD6C08B18D061/overview');
      browser.waitForAngular();
      expect(browser.getCurrentUrl()).toMatch(/.*#\/domain\/properties\/C3C54C52C53D41FA999BD6C08B18D061\/overview.*$/);
      expect(propertyDetailsPage.tabBar().isPresent()).toBeTruthy();
      expect($('.customer-data-panel').isPresent()).toBeTruthy();
    });
  
    it('should change tab to documents page', function() {
      browser.get('#/domain/properties/C3C54C52C53D41FA999BD6C08B18D061/overview');
      browser.waitForAngular();
      expect(browser.getCurrentUrl()).toMatch(/.*#\/domain\/properties\/C3C54C52C53D41FA999BD6C08B18D061\/overview.*$/);
      expect(propertyDetailsPage.tabBar().isPresent()).toBeTruthy();
      propertyDetailsPage.tabPropertyDocuments().click();
      expect($('.customer-data-panel').isPresent()).toBeFalsy();
      expect($('.print-documents-panel').isPresent()).toBeTruthy();
    });
  
    it('should disable documents panel buttons if no MainProductConfiguration is available', function () {
      propertyDetailsPage.get(andromeda.it, andromeda.contInf_010114_311214);
      propertyDetailsPage.tabPropertyDocuments().click();
      expect(propertyDetailsPage.documentsPanel().missingMainProductConfigurationForDcuments().isDisplayed()).toBe(true);
      expect(propertyDetailsPage.documentsPanel().missingMainProductConfigurationForDcuments().getText()).toContain('Kein Hauptprodukt "Abrechnen"');
      expect(propertyDetailsPage.documentsPanel().btnCostUserListPdf().isDisplayed()).toBe(true);
      expect(propertyDetailsPage.documentsPanel().btnCostUserListPdf().getAttribute('disabled')).toBeTruthy();
      expect(propertyDetailsPage.documentsPanel().btnRunServiceInvoiceProcessStart().isDisplayed()).toBe(true);
      expect(propertyDetailsPage.documentsPanel().btnRunServiceInvoiceProcessStart().getAttribute('disabled')).toBeTruthy();
    });
  });
});
