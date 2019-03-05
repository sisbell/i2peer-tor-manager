package org.i2peer.tor.manager

/**
 * Annotates TorConfigBuilder methods. When TorConfigBuilder.updateAllSettings is invoked, it will use this annotation
 * to automatically detect and use the associated method as part of building the config.
 *
 * The annotated method must have a no args.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class SettingsConfig
