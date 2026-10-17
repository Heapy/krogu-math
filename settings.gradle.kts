pluginManagement { repositories { gradlePluginPortal(); mavenCentral() } }
dependencyResolutionManagement { repositories { mavenCentral() } }
rootProject.name = "krogu-math"

include(":benchmarks")

// Opt-in unpublisheddiagnostic: ordinary builds/527 data and library artifacts are unchanged.
if (providers.gradleProperty("multiplication539").orNull == "true") include(":multiplication539")

// Private original-source worker compiler, not published as a consumer dependency.
include(":nodeWorker539")
