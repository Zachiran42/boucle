# Validation de Boucle 1.1.1

Vérifications du 27 octobre 2026 :

- 231 contrôles de régression du moteur passent : polygones, bords, intersections, collisions, score, combos, vies et mode libre.
- Chaque niveau exige un débit de capture supérieur d’au moins 40 % à celui de 1.1.0. Aucun niveau ne commence sans obstacles ni sans objectif de groupe ou de série.
- 5855 contrôles de campagne passent, dont une simulation déterministe qui termine chacun des50 niveaux avec ses vrais objectifs. Noms et règles distincts, dispositions reproductibles, victoires, échecs au chrono, étoiles, déblocages, sauvegarde et limite du niveau 50 sont vérifiés.
- 10 contrôles JVM du composant de tracé et 9 contrôles de campagne exécutent le vrai GameView avec des shims : captures, verrouillage, navigation vers le niveau suivant, record libre conservé et notification de fin unique.
- Rendus JVM/AWT examinés en 390×560, 390×650 et 390×780 : accueil, jeu, pause, victoire, échec et fin de campagne. Le chrome natif est simulé et les métriques typographiques approximatives.
- Compilation réelle Android SDK 36/Build Tools 36.0.0/JDK 21. APK universel pour Android8+, version 1.1.1/code3, même certificat que1.0.0.

Ces tests prouvent la cohérence des règles et la possibilité de terminer les objectifs dans la simulation. L’équilibrage ressenti, les gestes, sons, insets, sauvegarde Android et performances sur téléphone restent à valider sur un appareil réel. Aucun test téléphone ou émulateur n’est revendiqué.

Scripts de tests : scripts/test.ps1. Le harness de rendu et de gestes JVM est un outil de travail distinct des sources de l’APK.