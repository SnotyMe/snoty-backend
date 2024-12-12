plugins {
	id("com.osacky.doctor")
}

doctor {
	javaHome {
		failOnError.set(false)
		extraMessage.set("This may be a bug in gradle-doctor: https://github.com/runningcode/gradle-doctor/issues/187")
	}

}
