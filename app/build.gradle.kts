plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.googleService )
}

android {
    namespace = "com.example.southsidegrx_tfg"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.southsidegrx_tfg"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.lottie) //lottie splash screen animado
    implementation(libs.firebaseAuth) //Autenticación con firebase
    implementation(libs.firebaseDatabase) //Base de datos firebase
    implementation(libs.imagePicker) //libreria imagePicker
    implementation(libs.glide) //PARA MOSTRAR LAS IMAGENES SELECCIONADAS DENTRO DE CADA ELEMENTO/ leer imagenes
    implementation(libs.storage) //para subir imágenes o archivos multimedia
    //implementation(libs.authGoogle) //Iniciar sesión con google
    implementation(libs.ccp) // seleccionar nuestro código telefónico x pais
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}