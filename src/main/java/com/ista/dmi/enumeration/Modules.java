package com.ista.dmi.enumeration;

public enum Modules {
  COMMON("common", "common\\common-dbmigrations"),
  CDS("cds-parent", "cds-parent\\cds-dbmigrations"),
  CUS("cus-parent", "cus-parent\\cus-dbmigrations"),
  PDS("pds-parent", "pds-parent\\pds-dbmigrations"),
  IBS("ibs-parent", "ibs-parent\\ibs-dbmigrations"),
  PCS("pcs-parent", "pcs-parent\\pcs-dbmigrations"),
  MDR("mdr-parent", "mdr-parent\\mdr-dbmigrations");

  private String modulePath;
  private String dbMigrationPath;

  Modules(String modulePath, String dbMigrationPath) {
    this.modulePath = modulePath;
    this.dbMigrationPath = dbMigrationPath;
  }

  public String getModulePath() {
    return modulePath;
  }

  public void setModulePath(String modulePath) {
    this.modulePath = modulePath;
  }

  public String getDbMigrationPath() {
    return dbMigrationPath;
  }

  public void setDbMigrationPath(String dbMigrationPath) {
    this.dbMigrationPath = dbMigrationPath;
  }

  public String getTargetModulePath() {
    return String.format("%s\\%s\\%s", getModulePath(), this.name(),  "target");
  }
  public String getTargetDbMigrationPath() {
    return String.format("%s\\%s", getDbMigrationPath(), "target");
  }
}
