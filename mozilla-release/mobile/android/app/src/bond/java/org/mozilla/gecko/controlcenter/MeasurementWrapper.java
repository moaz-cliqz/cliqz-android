package org.mozilla.gecko.controlcenter;

/**
 * Copyright © Cliqz 2018
 *
 * Model class for values in dashboard. It contains the value itself and a corresponding unit;
 */
class MeasurementWrapper {

    private final String mValue;
    private final String mUnit;

    MeasurementWrapper(String value, String unit) {
        mValue = value;
        mUnit = unit;
    }

    String getValue() {
        return mValue;
    }

    String getUnit() {
        return mUnit;
    }
}
