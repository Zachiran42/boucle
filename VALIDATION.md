# Validation de Boucle 1.0.0

Vérifications effectuées le 27 octobre 2026 :

- 231 contrôles du moteur Java passent : géométrie concave et convexe, bords, intersections, collisions, scores, combos, vies, respawn et fin de partie.
- 10 contrôles supplémentaires exécutent le vrai GameView dans un environnement JVM avec shims : capture par tracé, annulation hors terrain, multitouch, interruption, pause/reprise et fin unique.
- Rendus de contrôle JVM/AWT des écrans accueil, jeu, pause et résultat examinés en 390×560, 390×650 et 390×780. Ces rendus approchent les métriques Android ; ils ne sont pas des captures de téléphone.
- Compilation réelle avec Android SDK36, Build Tools36.0.0 et JDK21 ; DEX pour Android8+.
- APK universel aligné et signature APK v2/v3 vérifiée. Identité fr.cercle.boucle, versionName1.0.0, versionCode1.
- Aucune permission Android, connexion réseau, publicité, compte ou service tiers dans le jeu.

L’installation, les gestes, les sons, les insets système et les performances sur un appareil Android réel restent à valider. Aucun test téléphone ou émulateur n’est revendiqué.

Les tests du moteur sont dans tests/GameEngineTest.java ; le script scripts/test.ps1 les exécute sans Android. Le harness visuel de contrôle est un outil de travail distinct du jeu.