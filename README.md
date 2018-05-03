# HTI projet

## Sujet :
> Pour la compensation de mouvement, on utilisera une méthode de block matching :  
> Pour chq bloc dans la trame t, on détermine un unique vecteur de déplacement (u, v) tel que :  
> ![img](http://latex.codecogs.com/svg.latex?%28u%2Cv%29%3D%5Cunderset%7B%28i%2Cj%29%7D%7Bargmin%7D%5Cleft%5C%7B%5Csum_%7Bk%3Di_0%7D%5E%7Bi_0%2B7%7D%5Csum_%7Bl%3Dj_0%7D%5E%7Bj_0%2B7%7D%5Cleft%20%7C%20x%5Et%28k%2Cl%29-x%5E%7Bt-1%7D%28k-i%2Cl-j%29%20%5Cright%20%7C%5Cright%5C%7D%2C%5Cqquad%20i%2Cj%3D-16%2C...%2C16)

> avec ![img](http://latex.codecogs.com/svg.latex?%28i_0%2Cj_0%29) les coordonnées du coin supérieur gauche du bloc considéré, et ![img](http://latex.codecogs.com/svg.latex?x%5Et) la trame t.

> La prédiction d'un pixel (i,j) de la trame t appartenant à un bloc de vecteur de déplacement (u,v) est donnée par la relation suivante :  
> ![img](http://latex.codecogs.com/svg.latex?%5Cwidehat%7Bx%7D%5Et%28i%2Cj%29%3Dx%5E%7Bt-1%7D%28i-u%2Cj-v%29)

1. Prédiction temporelle avec et sans estimation/compensation de mouvement.
- Première trame = Intra : pas de prédiction temporelle
- Trames suivantes = Prédictif : trame (t) prédite à partir de trame (t-1)

2. Transformée DCT blocs 8*8 des erreurs de prédiction.  
Quantification coef. DCT:

![img](http://latex.codecogs.com/svg.latex?%5Cwidehat%7Bf%7D_q%28u%2Cv%29%3D%5Cfrac%7B%5Cfrac%7B%5Cwidehat%7Bf%7D%28u%2Cv%29%5Ccdot+16%7D%7Bw%5Bu%5D%5Bv%5D%7D-k%5Ccdot+Q_s%7D%7B2Q_s%7D)

avec

![img](http://latex.codecogs.com/svg.latex?k%3D%5Cleft%5C%7B%5Cbegin%7Bmatrix%7D0%2C%26%5Ctext%7Bpour%20les%20blocs%20intra%7D%5C%5Csigne%5Cleft%5C%7B%5Cwidehat%7Bf%7D%28u%2Cv%29%5Cright%5C%7D%2C%26%5Ctext%7Bpour%20les%20blocs%20predits%7D%5Cend%7Bmatrix%7D%5Cright.)

![img](http://latex.codecogs.com/svg.latex?w%3D%5Cbegin%7Bbmatrix%7D8%2617%2618%2619%2621%2623%2625%2627%5C%5C17%2618%2619%2621%2623%2625%2627%2628%5C%5C20%2621%2622%2623%2624%2626%2628%2630%5C%5C21%2622%2623%2624%2626%2628%2630%2632%5C%5C22%2623%2624%2626%2628%2630%2632%2635%5C%5C23%2624%2626%2628%2630%2632%2635%2638%5C%5C25%2626%2628%2630%2632%2635%2638%2641%5C%5C27%2628%2630%2632%2635%2638%2641%2645%5Cend%7Bbmatrix%7D) la matrice de pondération.

![img](http://latex.codecogs.com/svg.latex?Q_s%5Cin%5C%7B1%2C...%2C31%5C%7D) : la taille/échelle du quantificateur.

> Pour 3. et 4., on utilise les coefficients de prédiction suivants :

> | 0   | 0.5 
> ------|-----
> | 0.5 |  x  

3. Prédiction DPCM ligne par ligne des coefficients DC de chaque DCT.
4. Codage prédictif (DPCM) des vecteurs de mouvement.
5. Estimation des entropies des trois sources à coder :
	* Coeff. DCT
	* Erreurs de prédiction des vecteurs de mouvement
	* Erreurs de prédiction des coeff. DC

## Remarques :
1. Le codage prédictif sans compensation de mouvement est équivalent à un codage avec la compensation de mouvement avec (u,v)=(0,0) pour chaque bloc de l'image.
2. Effectuez toujours la prédiction à partir des **images reconstruites**, et non à partir des images initiales.
