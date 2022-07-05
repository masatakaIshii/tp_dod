package grx.dod.demo.ihm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.UIManager;

import grx.dod.demo.tp.Couleur;
import grx.dod.demo.tp.ProcessingMode;
import grx.dod.demo.tp.Scenario;
import grx.dod.demo.tp.Type;
import grx.dod.demo.tp.types.objects.Cercle;
import grx.dod.demo.tp.types.objects.Conversion;
import grx.dod.demo.tp.types.objects.Emission;
import grx.dod.demo.tp.types.objects.Espace;
import grx.dod.demo.tp.types.objects.Filtre;
import grx.dod.demo.tp.types.objects.Forme;
import grx.dod.demo.tp.types.objects.Generation;
import grx.dod.demo.tp.types.objects.Mutation;
import grx.dod.demo.tp.types.objects.Rectangle;
import grx.dod.demo.tp.types.objects.Sauvegarde;
import grx.dod.demo.tp.types.objects.Tache;

public class TP {
	
	public static Principale principale(List<Forme> formes) {
		return new Principale("Espace d'occupation des formes", formes);
	}
	
	public static void draw(List<Forme> formes) {
		Principale window = principale(formes);
		
		window.setVisible(true);
	}
	
	public static void draw(String chemin) {
		// Chargement classique
		List<Forme> formes = Objects.charger(chemin);
		
		draw(formes);
	}
	
	public static long start() {
		return System.currentTimeMillis();
	}
	
	public static long end(long start) {
		long end = System.currentTimeMillis();
		
		return (end-start);
	}
	
	public static int randomX() {
		// -10 ... +10
		return (int)((Math.random()*20)-10);
	}
	
	public static int randomY() {
		// -10 ... +10
		return (int)((Math.random()*20)-10);
	}
	
	public static int randomRadius() {
		// 0 ... 10
		return (int)(Math.random()*10);
	}
	
	public static int randomWidth() {
		// 0 ... 10 | 11
		return (int)(Math.random()*10+1);
	}
	
	public static int randomHeight() {
		// 0 ... 10 | 11
		return (int)(Math.random()*10+1);
	}
	
	public static Couleur randomColor() {
		int count = Couleur.values().length;
		int order = (int)(Math.random()*count);
		
		return Couleur.get(order);
	}

	public static void main(String[] args) throws Exception {
		String chemin = "src\\main\\resources\\espace.txt";
		List<Forme> formes = Objects.charger(chemin);
		
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		draw(formes);
	}
	
	public static class Objects {
		
		private Objects() {}
		
		private static List<Forme> formes;
		private static Espace espace;
		
		public static List<Forme> charger(String chemin) {
			formes = (new Generation(chemin)).objects();
			return formes;
		}
		
		public static void sauvegarder(String chemin, List<Forme> formes) {
			(new Sauvegarde(chemin)).ecrire(formes);
		}
		
		public static List<Forme> formes() {
			if (formes==null) {
				formes = new ArrayList<>();
			}
			return formes;
		}
		
		public static Espace espace() {
			if (espace==null) {
				espace = espace(formes());
			}
			return espace;
		}

		public static Espace espace(List<Forme> formes) {
			if (formes!=null && !formes.isEmpty()) {
				List<Forme> rects = new ArrayList<>();
				Conversion conversion = new Conversion();
				
				for (Forme forme : formes) {
					if (forme instanceof Rectangle) {
						// Rien à faire
						rects.add(forme);
					} else if (forme instanceof Cercle) {
						// Conversion à faire
						rects.add(conversion.apply(forme));
					} else {
						// On ne sait pas faire
					}
				}
				
				Emission espace = new Emission();
				
				return (Espace)espace.output(rects).get(0);
			} else {
				// Rien
				return null;
			}
		}

		public static Espace pipeline(List<Forme> formes) {
			if (formes!=null && !formes.isEmpty()) {
				Emission emission = new Emission();
				Conversion conversion = new Conversion();
				Mutation mutation = new Mutation(conversion);
				
				List<Forme> s1;
				List<Forme> s2;
				
				s1 = emission.output(mutation.output(Filtre.output(Type.CERCLE,    formes)));
				s2 = emission.output(               (Filtre.output(Type.RECTANGLE, formes)));
				
				List<Forme> sN = new ArrayList<>();
				sN.addAll(s1);
				sN.addAll(s2);
				
				return (Espace)emission.output(sN).get(0);
			} else {
				// Rien
				return null;
			}
		}

		public static Espace parallel(List<Forme> formes, int nbThreads) {
			if (formes!=null && !formes.isEmpty()) {
				Emission emission = new Emission();
				Conversion conversion = new Conversion();
				Tache mutation;
				
				ExecutorService processeur = Executors.newFixedThreadPool(nbThreads);
				
				List<Future<Forme>> taches = new ArrayList<>();
				for (Forme forme : formes) {
					if (forme instanceof Rectangle) {
						// Pas de conversion
						mutation = new Tache(forme);
					} else {
						// Avec conversion
						mutation = new Tache(forme, conversion);
					}
					taches.add(processeur.submit(mutation));
				}
				
				List<Forme> espace = new ArrayList<>();
				for (Future<Forme> tache : taches) {
					try {
						espace.add(tache.get());
					} catch (Exception failure) {
						// Erreur !
						failure.printStackTrace();
					}
				}
				
				processeur.shutdown();
				
				return (Espace)emission.output(espace).get(0);
			} else {
				// Rien
				return null;
			}
		}
		
	}
	
	public static class Generiques {
		
		private Generiques() {}
		
		// Conversion :
		// Structure objet => Structure générique
		public static Object generique(Forme forme) {
			// TODO : ...
			return null;
		}
		
		public static List<?> generiques(List<Forme> formes) {
			// TODO : ...
			return null;
		}
		
		// Conversion :
		// Structure générique => Structure typée
		public static Forme forme(Object generique) {
			// TODO : ...
			return null;
		}
		
		public static List<?> formes(List<?> generiques) {
			// TODO : ...
			return null;
		}
		
		public static Scenario scenario() {
			return scenario(ProcessingMode.PIPELINE, 0);
		}
		
		public static Scenario scenario(int nbThreads) {
			return scenario(ProcessingMode.PARALLEL, nbThreads);
		}
		
		private static Scenario scenario(ProcessingMode processing, int nbThreads) {
			// TODO : suivant le mode de processing
			return null;
		}

		
		// TODO : ...
		// - pipeline
		// - parallel
		
	}
	
	public static class Simples {
		
		private Simples() {}
		
		// Conversion :
		// - Structure typée => Structure simplifiée
		public static Object simple(Forme forme) {
			// TODO : ...
			return null;
		}
		
		public static List<?> simples(List<Forme> formes) {
			// TODO : ...
			return null;
		}
		
		// Conversion :
		// - Structure simplifiée => Structure typée
		public static Forme forme(Object simple) {
			// TODO : ...
			return null;
		}
		
		public static List<Forme> formes(List<?> simples) {
			// TODO : ...
			return null;
		}
		
		// TODO : ...
		// - pipeline
		// - parallel
		
	}
	
}
