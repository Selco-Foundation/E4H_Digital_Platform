package org.egov.asset.util;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class AssetConstants {

    public static final String ASSET_MODULE_NAME = "asset-registry";

    public static final String ASSET_COUNT_CODE = "AssetCountSchema";
    public static final String ASSET_TYPE_C0DE = "AssetTypeSchema";
    public static final String BRAND_CODE = "BrandSchema";
    public static final String SYSTEM_CODE = "SystemSchema";
    public static final String WARRANTY_DURATION = "WarrantyDurationSchema";

    public static final String SYSTEM_DC = "DC";
    public static final String SYSTEM_AC_OFF_GRID = "AC Off Grid";
    public static final Set<Double> VALID_DC_BATTERY_CAPACITIES = new HashSet<>(
            Arrays.asList(125.0, 150.0, 180.0, 200.0, 220.0));
    public static final Set<Double> VALID_DC_BATTERY_VOLTAGES = new HashSet<>(
            Arrays.asList(12.0, 48.0, 51.2));
    public static final Set<Double> VALID_AC_BATTERY_VOLTAGES = new HashSet<>(
            Arrays.asList(12.0, 12.8, 24.0));
    public static final Set<Double> VALID_TOTAL_CAPACITIES = new HashSet<>(
            Arrays.asList(1.8, 3.6, 4.8, 9.6, 14.4, 17.2, 24.0, 21.6, 19.2));
    public static final Set<Double> VALID_INVERTER_CAPACITIES = new HashSet<>(
            Arrays.asList(1.0, 3.0, 5.0, 6.0, 7.5, 10.0));
    public static final Set<String> VALID_BATTERY_TYPES = new HashSet<>(
            Arrays.asList("Lithium", "Lead Acid"));

}
