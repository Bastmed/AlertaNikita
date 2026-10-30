Aplicación nativa para Android desarrollada en Kotlin diseñada para automatizar la recolección y el canje de códigos promocionales de Escape from Tarkov y Tarkov Arena.

El sistema actúa como un servicio en segundo plano que monitorea las notificaciones push de la cuenta oficial de Nikita Buyanov en X (Twitter). Mediante un análisis de texto impulsado por expresiones regulares, extrae los códigos ocultos en los tweets, los copia al portapapeles, emite una alerta crítica forzando el volumen máximo del dispositivo y abre automáticamente el navegador en la URL exacta de canje.

Características Principales
Detección Inteligente: Clasifica automáticamente entre llaves estándar de Tarkov, palabras promocionales, alertas de Arena y avisos sin código utilizando Regex y validación de contexto.

Tolerancia a Formatos: Capacidad para extraer códigos a los que les faltan caracteres (reemplazados por asteriscos) o que poseen arquitecturas irregulares (bloques de 4 o 5 caracteres).

Enrutamiento Automático: Identifica si la recompensa pertenece al juego base o a Tarkov Arena y abre el subdominio web correspondiente para evitar pérdidas de tiempo.

Overdrive de Audio: Intercepta el hardware del dispositivo para reproducir el tono de alerta al 100% de volumen, garantizando que el usuario despierte o preste atención de inmediato, utilizando patrones de vibración distintivos para cada tipo de código.

Interfaz de Configuración Rápida: Panel de control que permite asignar sonidos personalizados a cada tipo de alerta utilizando los archivos multimedia locales del dispositivo, con accesos directos integrados para gestionar los permisos críticos del sistema.
