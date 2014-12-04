package com.viorsan.dollmaster;

import java.util.HashMap;

/**
 * Created by dkzm on 24.05.14.
 * originally based off https://developer.android.com/samples/BluetoothLeGatt/src/com.example.android.bluetoothlegatt/SampleGattAttributes.html
 */

/**
 * GATT Attributes
 * Only ones which are needed to me are here
 * also see https://www.bluetooth.org/en-us/specification/assigned-numbers
 */
public class GattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String BODY_SENSOR_LOCATION = "00002a38-0000-1000-8000-00805f9b34fb";
    public static String MANUFACTURER_NAME_STRING="00002a29-0000-1000-8000-00805f9b34fb";
    public static String BATTERY_POWER_STATE = "00002a1a-0000-1000-8000-00805f9b34fb";
    public static String SERIAL_NUMBER_STRING = "00002a25-0000-1000-8000-00805f9b34fb";
    public static String SYSTEM_ID = "00002a23-0000-1000-8000-00805f9b34fb";
    public static String PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS="00002a04-0000-1000-8000-00805f9b34fb";
    public static String DEVICE_NAME="00002a00-0000-1000-8000-00805f9b34fb";
    public static String APPEARANCE="00002a01-0000-1000-8000-00805f9b34fb";
    public static String BATTERY_LEVEL="00002a19-0000-1000-8000-00805f9b34fb";
    public static String BATTERY_LEVEL_STATE="00002a1b-0000-1000-8000-00805f9b34fb";
    public static String FIRMWARE_REVISION_STRING="00002a26-0000-1000-8000-00805f9b34fb";
    public static String HARDWARE_REVISION_STRING="00002a27-0000-1000-8000-00805f9b34fb";


    //services
    public static String HEART_RATE_SERVICE                ="0000180d-0000-1000-8000-00805f9b34fb";
    //https://developer.bluetooth.org/gatt/services/Pages/ServiceViewer.aspx?u=org.bluetooth.service.heart_rate.xml

    public static String GATT_SERVICE                      ="00001801-0000-1000-8000-00805f9b34fb";
    public static String DEVICE_INFORMATION_SERVICE        ="0000180a-0000-1000-8000-00805f9b34fb";
    public static String BATTERY_SERVICE                   ="0000180f-0000-1000-8000-00805f9b34fb";
    public static String CYCLING_POWER_SERVICE             ="00001818-0000-1000-8000-00805f9b34fb";
    public static String CYCLING_SPEED_AND_CADENCE_SERVICE ="00001816-0000-1000-8000-00805f9b34fb";
    public static String GLUCOSE_SERVICE                   ="00001808-0000-1000-8000-00805f9b34fb";
    public static String RUNNING_SPEED_AND_CADENCE_SERVICE ="00001814-0000-1000-8000-00805f9b34fb";
    public static String BLOOD_PRESSURE_SERVICE            ="00001810-0000-1000-8000-00805f9b34fb";
    public static String HEALTH_THERMOMETER_SERVICE        ="00001809-0000-1000-8000-00805f9b34fb";
    public static String GENERIC_ACCESS_SERVICE            ="00001800-0000-1000-8000-00805f9b34fb";


    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";





    static {
        // Services.
        attributes.put(HEART_RATE_SERVICE, "Heart Rate Service");
        attributes.put(DEVICE_INFORMATION_SERVICE, "Device Information Service");
        attributes.put(GATT_SERVICE, "Generic Attribute Service");
        attributes.put(BATTERY_SERVICE, "Battery Service");
        attributes.put(CYCLING_POWER_SERVICE,"Cycling Power Service");
        attributes.put(CYCLING_SPEED_AND_CADENCE_SERVICE,"Cycling Speed and Cadence Service");
        attributes.put(GLUCOSE_SERVICE,"Glucose Service");
        attributes.put(RUNNING_SPEED_AND_CADENCE_SERVICE,"Running Speed and Cadence Service");
        attributes.put(BLOOD_PRESSURE_SERVICE,"Blood Pressure Service");
        attributes.put(HEALTH_THERMOMETER_SERVICE,"Health Thermometer Service");
        attributes.put(GENERIC_ACCESS_SERVICE,"Generic Access Service");

        // Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put(MANUFACTURER_NAME_STRING, "Manufacturer Name String");
        attributes.put(BODY_SENSOR_LOCATION,"Body Sensor Location");
        attributes.put(BATTERY_LEVEL,"Battery Level");
        attributes.put(BATTERY_LEVEL_STATE,"Battery Level State");
        attributes.put(BATTERY_POWER_STATE,"Battery Power State");
        attributes.put(SERIAL_NUMBER_STRING,"Serial Number String");
        attributes.put(SYSTEM_ID,"System ID");
        attributes.put(PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS,"Peripheral Preferred Connection Parameters");
        attributes.put(DEVICE_NAME,"Device Name");
        attributes.put(APPEARANCE,"Appearance");
        attributes.put(FIRMWARE_REVISION_STRING,"Firmware Revision String");
        attributes.put(HARDWARE_REVISION_STRING,"Hardware Revision String");




    }



    public static String lookup(String uuid) {
        String name = attributes.get(uuid);
        return name == null ? uuid : name;
    }
}