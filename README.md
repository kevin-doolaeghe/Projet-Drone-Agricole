# drone
Application Android pour le projet Drone Agricole


25/02
1 version
Création du projet
Installation d’Android Studio

28/02
1 version
Test unitaire enregistrement SDK (MainActivity)
Plantage : classe Application à lier à l’application dans le manifest

01/03
1 version
Interface de démarrage
Toast, organisation des éléments. Aide Enzo (Création image)

03/03
1 version
Programme
Aide Enzo (Gestion du programme & organisation)

04/03
1 version
Test unitaire communication drone
Classe Aircraft, aide documentation DJI, exemples DJI..

05/03
1 version
Interface de base
Organisation du code, nomenclature...

06/03
1 version
Tests pour trames HTTP
Recherche méthodes et/ou bibliothèque => Volley, HTTPClient, HttpUrlConnection..
Ligne à ajouter dans le manifest pour envoyer des requêtes HTTP !!

09/03
1 version
Test unitaire communication REST
Aide Nicolas (REST) et Enzo (Communication)

10/03
1 version
Interface
TabLayout, ViewPager et Adapter, MenuInflater (Override de onCreateOptionsMenu..)

11/03
1 version
Classe Aircraft
Plantage

12/03
3 versions
Interface (FlightPlansActivity, MenuActivity) & classe Aircraft
RecyclerView pour liste, AsyncTask (Thread), Handler

13/03
2 versions
Algorithme drone, missions, arrangement et interface pour l’expédition
Démarrage impossible : Erreur à trouver !

14/03
2 versions
Test de lancement de mission, amélioration des méthodes et de l’interface
GPS : EN EXTERIEUR !!
Méthode createTestMission().
TextView de debug ajoutés

18/03
2 versions
Test unitaire pilotage drone.
Arrangement du code
Mission test : OK.
Test algorithme. Problèmes.. 

19/03
1 version
Requêtes HTTP - Upload d’une image : OK
Mauvaise réception du fichier & aide d'Enzo

20/03
1 version
Requêtes HTTP - Téléchargement d’une image : OK
Algorithme vol drone
Mauvais contenu du fichier et aide Nicolas.
Etude de la classe AsyncTask

21/03
1 version
Test unitaire de l'algorithme du drone
Fonctionne globalement

23/03
1 version
Arrangement code. Commentaires

25/03
1 version
Test algorithme de vol : OK
Plantages qui surviennent à cause du GPS : à résoudre
Les points sont bien calculés

26/03
1 version
Algorithme drone

27/03
1 version
Tests de l’algorithme

28/03
2 versions
Amélioration algorithme drone
Images qui se superposent

01/04
1 version
Refonte algorithme drone
Algorithme repensé

02/04
2 versions
Requêtes HTTP
Choix entre Retrofit et Volley
Passage à Volley : flexible et efficace

03/04
1 version
Requêtes HTTP
Aide Nicolas & Enzo : Rest v3 avec implémentation du programme d’Enzo

04/04
1 version
Requêtes HTTP : Tests
Test de Volley

23/04
1 version
Requêtes HTTP : Tests
Test de Retrofit

24/04
2 versions
Refonte de l'application et classes pour requêtes HTTP
Animations, SwipeRefreshLayout,... Préparation des classes Repository, RestApi (Retrofit).

25/04
1 version
Architecture MMVM de l'application
Retrofit et Volley : Idéal pour architecture MVVM (Model-View-ViewModel) et Android Jetpack.
Etude documentation Android.

28/04
2 versions
MVVM fonctionnel.
Test unitaire requête REST et affichage des plans (CRUD) focntionnel.
Problème : 3 jours de recherches pour juste de mauvaises dépendences installées... mais fonctionnel à terme : Architecture de code -> Ok (pas MVC, MVP mais MVVM)

29/04
2 versions
Restructuration appli : ok (version 2)
Problème d'intégration : AndroidX (Jetpack) et le SDK DJI ne sont pas compatibles

30/04
1 version
Fin restructuration appli, choix plan de vol..
Classe Drone refaite

02/05
2 versions
Application fonctionne correctement. Corrections & diagrammes de classe.
Vol du drone : ok
Gestion PDV : ok
Envoi des images : à faire
Démarrage de la synthèse : ok
Récupération de l’image finale : ok

06/05
1 version
Recherche pour problème des points. (photos)

07/05
1 version
Téléchargement des fichiers du drone.
Echec : à revoir

08/05
1 version
Ajout de constantes textuelles.
Optimisation.
Refonte des interfaces (.XML).
Refonte de l’écran de chargement.
L’API Maps est payant et ne peut donc être utilisé.

12/05
1 version
Meilleure implémentation des classes ViewModel.
Les classes de gestion du drone sont désormais des singletons.
Récupération des fichiers, ajout des paramètres..
Mauvaise compréhension du partage de ViewModel entre Fragment..

15/05
1 version
Mise à jour du service Rest et quelques bugs corrigés

21/05
1 version
Migration vers Volley, Mise à jour des classes ViewModel et Repository
Volley : Classe Multipart à ajouter

22/05
1 version
Impémentation des requêtes HTTP avec Volley & Gson

23/05
1 version
Tests Volley.
Test des classes du drone.
Dessin du trajet.
Les requêtes HTTP fonctionnent.
Dessin du trajet à terminer.
