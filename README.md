# PayTrack 💳📅

**PayTrack** es una aplicación nativa para Android desarrollada en **Kotlin** y **Jetpack Compose** diseñada para gestionar suscripciones, gastos recurrentes, pagos puntuales, ingresos y objetivos mensuales de ahorro de forma visual mediante un calendario interactivo.

---

## ✨ Características principales

* **Calendario Interactivo Mensual:** Visualiza de un vistazo qué días del mes tienen movimientos programados, navega entre meses y consulta los detalles de cada día con un toque.
* **Gestión Completa de Movimientos (Ingresos y Gastos):**
  * **Mensuales:** Pagos o ingresos recurrentes todos los meses.
  * **Únicos:** Movimientos puntuales que solo aplican en el mes y año seleccionados.
  * **Flexibles:** Selección personalizada de meses específicos del año (ej. impuestos trimestrales, seguros semestrales o pagas extra).
* **Categorías Dinámicas y Filtrado:** Crea, asigna y elimina categorías personalizadas (*Gastos fijos*, *Ocio*, *Suscripciones*, etc.) y filtra la lista mensual para analizar el gasto desglosado por cada una.
* **Control Financiero y Objetivo de Ahorro Mensual:**
  * Cálculo automático de **Ingresos**, **Gastos**, importe **Pagado / Restante** del mes en curso y **Balance Neto**.
  * Barra de progreso en tiempo real que mide si la diferencia entre ingresos y gastos alcanza tu meta de ahorro mensual configurada.
* **Recordatorios Inteligentes:** Notificaciones locales diarias mediante `BroadcastReceiver` que alertan únicamente de los gastos programados para el día actual, respetando las reglas de recurrencia y los ajustes de fin de mes.
* **Copias de Seguridad Portables (JSON):** Exportación e importación de todos los datos (movimientos, categorías y objetivo de ahorro) mediante el *Storage Access Framework* de Android para migrar fácilmente entre dispositivos.
* **Personalización Visual:** Selector integrado de **Tema Claro / Oscuro** y paleta de colores identificativos para cada movimiento con ajuste automático de contraste de texto según la luminancia.

---

## 🛠️ Stack Tecnológico y Arquitectura

El proyecto sigue las recomendaciones modernas de desarrollo en Android bajo el patrón **MVVM (Model-View-ViewModel)**:

* **Lenguaje:** Kotlin
* **UI:** Jetpack Compose con Material Design 3
* **Arquitectura:** MVVM + Flujo de datos reactivo con `StateFlow` y `collectAsStateWithLifecycle`
* **Inyección de Dependencias:** Dagger Hilt (`@HiltAndroidApp`, `@HiltViewModel`, `@AndroidEntryPoint`)
* **Persistencia Local:**
  * **Room Database:** Almacenamiento estructurado de movimientos (`PayTrackItem`) y categorías (`CategoryItem`) con compilación mediante **KSP**.
  * **SharedPreferences:** Persistencia ligera para la configuración del objetivo mensual de ahorro.
* **Serialización:** Gson para la exportación e importación de copias de seguridad en formato JSON.
* **Notificaciones:** `BroadcastReceiver` + `NotificationCompat` + Corrutinas (`Dispatchers.IO`).

---

## 📂 Estructura del Proyecto

```text
com.nicolascarmona.paytrack
├── data/               # Room Database, DAO y TypeConverters
├── di/                 # Módulos de inyección de dependencias con Hilt
├── model/              # Entidades de datos (PayTrackItem, CategoryItem, TimeUnit)
├── ui/                 # Pantallas, componentes Compose, diálogos y tema (Light/Dark)
├── viewmodel/          # PayTrackViewModel (estado reactivo, lógica de negocio y backups)
└── PaymentNotificationReceiver.kt  # Gestión de alertas diarias de pago
