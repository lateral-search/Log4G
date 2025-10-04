## Log4G - Gravimeter Observations Data Gathering, and Validation/Control Application

**Author:** Andres H. Pityla C.

Log4G is a mobile device application developed for the OSU - The Ohio State University using Java and Android. It is used to gather gravity measurements during field work. Log4G has a number of features that make it useful for gravity surveys, including:

* Log4G was specifically designed to comply with the OSU field protocol. (see Research Paper in Related Publications).
* The ability to load and use per-gravimeter calibration tables
* The ability to perform validations on field
* The ability to gather visual info, regular GPS location information, and gravity observations for each geographical point
* The ability to automatically create a return line from a forward line
* The ability to export gathered data for post analysis

Log4G is divided into a few modules, each of which is capable of performing different tasks. The modules include:

* Synchronization: This module creates a new clean session, backs up and drops the database, exports all data in the database to a spreadsheet, and creates other necessary files.
* Creation and modification of Lines: This module allows users to create and modify lines where geographical points will be loaded and observations taken.
* Creation and modification of points: This module allows users to create and modify geographical points during an expedition.
* Geolocation validation using in device GPS: This module uses the device GPS to retrieve preexistent geographical points depending on the user's location.
* Input of gravity observations for each of those points: This module allows users to input gravity observations for each geographical point.
* Automatic creation of a return line in base to the forward line and points associated: This module automatically creates a return line from a forward line.
* GPS to validate locations and reuse some closer to actual location points: This module uses the device GPS to validate locations and reuse some closer to actual location points.

Open source libraries used by Log4G

Log4G uses a number of open source libraries, including:

* Apache POI Java APIs: This library allows Log4G to read and write Excel files.
* SSJ 3.3.1 Stochastic Simulation in Java: This library provides function fit utilities for curve fitting and interpolation with polynomials.
* EJML Efficient Java Matrix Library: This library provides a linear algebra library for manipulating real/complex/dense/sparse matrices.
* Commons Math: The Apache Commons Mathematics Library: This library provides a library of lightweight, self-contained mathematics and statistics components.
* SQLite: This library provides a relational database engine.
* MPAndroidChart: This library provides a powerful Android chart view / graph view library.


Log4G v1.0 require the following dependencies and versions:

* ssj-3.2.1.jar (license: Apache v2.0)
* poi-3.9-20121203.jar (license: Apache v2.0)
* MPAndroidChart-v3.0.2.jar (license: Apache v2.0)
* commons-math3-3.6.1.jar (license: Apache v2.0)
* EJML.jar (license: Apache v2.0)

Log4G v1.0 actually was developed and clean compiled with the following versions:

* Android Studio 3.1.2 (powered by the ImtelliJ Platform)
* Using the embedded JDK
* Compile SDK version: API 27:Android 8.1 (Oreo)
* Min SDK version: 15
* Source and Target compatibility: 1.8
* Gradle version: 4.4
* Android Plugin version: 3.1.2
* JRE:1.8.0_152-release-1024-b02 amd64
* JVM: OpenJDK64-Bit Server VM by JetBrains s.r.o

**note:  Developed using the Android Studio IDE.**



Related publications:

* GRAVITAS: A Matlab package to compute the gravity differences between stations of multiple gravity lines, and combine them into a network adjustment Authors: Demián D. Gómez, Kevin Ahlgren, Michael G. Bevis https://github.com/demiangomez/GRAVITAS

* A robust approach to terrestrial relative gravity measurements and adjustment of gravity networks (2024). Sobrero, F.S., Ahlgren, K., Bevis, M.G., Gómez, D.D., Heck, J., Echalar, A., Caccamise, D.J., Kendrick, E., Montenegro, P., Batistti, A., Contreras Choque, L., Catari, J.C., Tinta Sallico, R., Guerra Trigo, H., Journal of Geodesy 98, 86. https://doi.org/10.1007/s00190-024-01891-w
