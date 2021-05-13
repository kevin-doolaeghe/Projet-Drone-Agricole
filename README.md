# Projet Drone Agricole

## Présentation

Application Android :

* Application Android utilisant le SDK DJI pour piloter un drone.  
* Communication avec un serveur RESTful pour échanger les images prises.  
* Architecture MVVC (Model-View-View Model) utilisée pour le projet.  

- - - - - - -

Serveur REST :

* Application Spring Boot.  
* Assemblage des images inclus.  

## Installation

Installer l'application `app-drone.apk` sur un appareil Android.

Lancement du serveur REST :

```
java -jar rest_drone.jar
```