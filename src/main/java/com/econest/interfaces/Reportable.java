package com.econest.interfaces;

import java.util.Map;

/**
 * Interface representing any business entity that contributes to financial
 * and operational reports & analytics in EcoNest.
 * 
 * Demonstrates: OOP Interface & Polymorphism in Reporting.
 */
public interface Reportable {
    /**
     * Unique identifier for the report item.
     * @return report identifier
     */
    String getReportIdentifier();

    /**
     * Numerical or monetary value contributed to aggregate reports.
     * @return double monetary/numerical value
     */
    double getMonetaryValue();

    /**
     * Converts the entity into a structured key-value map for report generators (CSV, PDF, Table).
     * @return map of report attributes
     */
    Map<String, Object> toReportRow();
}
