const { withAndroidManifest, withGradleProperties } = require('@expo/config-plugins');

module.exports = function withAndroidManifestFix(config) {
  // Fix AndroidManifest
  config = withAndroidManifest(config, async (config) => {
    const androidManifest = config.modResults;
    const mainApplication = androidManifest.manifest.application[0];

    // Add tools namespace if not present
    if (!androidManifest.manifest.$) {
      androidManifest.manifest.$ = {};
    }
    androidManifest.manifest.$['xmlns:tools'] = 'http://schemas.android.com/tools';

    // Add tools:replace for appComponentFactory
    if (!mainApplication.$) {
      mainApplication.$ = {};
    }
    mainApplication.$['tools:replace'] = 'android:appComponentFactory';
    mainApplication.$['android:appComponentFactory'] = 'androidx.core.app.CoreComponentFactory';

    return config;
  });

  // Enable AndroidX and Jetifier
  config = withGradleProperties(config, (config) => {
    const gradleProperties = config.modResults;
    
    // Ensure AndroidX is enabled
    const androidXIndex = gradleProperties.findIndex(
      (item) => item.type === 'property' && item.key === 'android.useAndroidX'
    );
    if (androidXIndex >= 0) {
      gradleProperties[androidXIndex].value = 'true';
    } else {
      gradleProperties.push({
        type: 'property',
        key: 'android.useAndroidX',
        value: 'true',
      });
    }

    // Enable Jetifier to migrate support libraries to AndroidX
    const jetifierIndex = gradleProperties.findIndex(
      (item) => item.type === 'property' && item.key === 'android.enableJetifier'
    );
    if (jetifierIndex >= 0) {
      gradleProperties[jetifierIndex].value = 'true';
    } else {
      gradleProperties.push({
        type: 'property',
        key: 'android.enableJetifier',
        value: 'true',
      });
    }

    return config;
  });

  return config;
};
