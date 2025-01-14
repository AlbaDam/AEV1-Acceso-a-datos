package Evaluable1;

import java.awt.BorderLayout;


import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import javax.swing.border.LineBorder;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;


/**
 * Classe Vista que representa la interfície gràfica per a la gestió de fitxers.
 */
public class Vista extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtDirectori;
	private JTextField txtParaula;
	private JTextField txtReemplaçar;
	private JTextArea txtAreaResultats;
	private JButton btnDirectori;
	private JButton btnParaula;
	private JButton btnReemplaçar;
	private JCheckBox chkAccents;
	private JCheckBox chkMayusMinus;

	/**
     * Mètode principal que inicia l'aplicació.
     * @param args Arguments de línia de comandes.
     */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Vista frame = new Vista();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Obre un diàleg per seleccionar un directori. Actualitza el camp de text amb
	 * la ruta del directori seleccionat.
	 */
	private void seleccionarDirectori() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int resultat = fileChooser.showOpenDialog(this);

		if (resultat == JFileChooser.APPROVE_OPTION) {
			File directoriSeleccionat = fileChooser.getSelectedFile();
			txtDirectori.setText(directoriSeleccionat.getAbsolutePath());
		}
	}

	/**
	 * Llista recursivament els fitxers i subdirectoris d'un directori.
	 * 
	 * @param directori El directori a llistar.
	 * @param prefix    Prefix per a la visualització.
	 * @param subPrefix Prefix addicional per a subdirectoris.
	 */
	private void llistarArbreFitxers(File directori, String prefix, String subPrefix) {
		File[] fitxers = directori.listFiles();

		if (fitxers != null && fitxers.length > 0) {
			for (int i = 0; i < fitxers.length; i++) {
				File fitxer = fitxers[i];
				boolean esUltim = (i == fitxers.length - 1);

				if (fitxer.isDirectory()) {
					txtAreaResultats.append(prefix + (esUltim ? "|-- " : "|-- ") + fitxer.getName() + "\n");
					llistarArbreFitxers(fitxer, subPrefix + (esUltim ? "    " : "|   "),
							subPrefix + (esUltim ? "    " : "|   "));
				} else {
					mostrarInformacioFitxer(fitxer, prefix + (esUltim ? "|-- " : "|-- "));
				}
			}
		}
	}

	/**
	 * Mostra informació detallada d'un fitxer.
	 * 
	 * @param fitxer El fitxer del qual mostrar la informació.
	 * @param prefix Prefix per a la visualització.
	 */
	private void mostrarInformacioFitxer(File fitxer, String prefix) {
		String nomFitxer = fitxer.getName();
		String extensio = obtenirExtensio(fitxer);
		double grandaria = fitxer.length() / 1024.0;
		String ultimaModificacio = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(fitxer.lastModified()));
		txtAreaResultats.append(
				prefix + nomFitxer + " (" + String.format("%.2f", grandaria) + " KB – " + ultimaModificacio + ")\n");
	}

	/**
	 * Obtén l'extensió d'un fitxer.
	 * 
	 * @param fitxer El fitxer del qual obtenir l'extensió.
	 * @return L'extensió del fitxer o "Sense extensió" si no en té.
	 */
	private String obtenirExtensio(File fitxer) {
		String nomFitxer = fitxer.getName();
		int index = nomFitxer.lastIndexOf(".");
		if (index > 0 && index < nomFitxer.length() - 1) {
			return nomFitxer.substring(index + 1);
		}
		return "Sense extensió";
	}

	/**
	 * Busca una paraula en l'estructura de fitxers d'un directori.
	 * 
	 * @param directori El directori en el qual buscar.
	 * @param paraula   La paraula a buscar.
	 * @param prefix    Prefix per a la visualització.
	 * @param subPrefix Prefix addicional per a subdirectoris.
	 */
	private void buscarParaulaEnArbre(File directori, String paraula, String prefix, String subPrefix) {
		File[] fitxers = directori.listFiles();

		if (fitxers != null && fitxers.length > 0) {
			for (int i = 0; i < fitxers.length; i++) {
				File fitxer = fitxers[i];
				boolean esUltim = (i == fitxers.length - 1);

				if (fitxer.isDirectory()) {
					txtAreaResultats.append(prefix + (esUltim ? "|-- " : "|-- ") + fitxer.getName() + "\n");
					buscarParaulaEnArbre(fitxer, paraula, subPrefix + (esUltim ? "    " : "|   "),
							subPrefix + (esUltim ? "    " : "|   "));
				} else {
					mostrarCoincidencies(fitxer, paraula, prefix + (esUltim ? "|-- " : "|-- "));
				}
			}
		}
	}

	/**
	 * Mostra les coincidències d'una paraula en un fitxer.
	 * 
	 * @param fitxer  El fitxer en el qual buscar.
	 * @param paraula La paraula a buscar.
	 * @param prefix  Prefix per a la visualització.
	 */
	private void mostrarCoincidencies(File fitxer, String paraula, String prefix) {
		int contador = 0;

		try (BufferedReader br = new BufferedReader(new FileReader(fitxer, StandardCharsets.UTF_8))) {
			String linia;
			while ((linia = br.readLine()) != null) {

				if (chkMayusMinus.isSelected()) {
					linia = convertirMinuscules(linia);
				}
				if (chkAccents.isSelected()) {
					linia = normalitzarSenseAccents(linia);
				}

				int indice = linia.indexOf(paraula);
				while (indice != -1) {
					contador++;
					indice = linia.indexOf(paraula, indice + 1);
				}
			}
		} catch (IOException e) {
			txtAreaResultats.append(prefix + "Error llegint el fitxer: " + fitxer.getName() + "\n");
		}

		txtAreaResultats.append(prefix + fitxer.getName() + " (" + contador + " coincidències)\n");
	}

	/**
	 * Normalitza un text per eliminar els accents.
	 * 
	 * @param text El text a normalitzar.
	 * @return El text sense accents.
	 */
	private String normalitzarSenseAccents(String text) {
		return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}

	/**
	 * Converteix un text a minúscules.
	 * 
	 * @param text El text a convertir.
	 * @return El text en minúscules.
	 */
	private String convertirMinuscules(String text) {
		return text.toLowerCase();
	}

	/**
	 * Busca i reemplaça una paraula en l'estructura de fitxers.
	 * 
	 * @param directori  El directori en el qual buscar.
	 * @param paraula    La paraula a buscar.
	 * @param reemplaçar La paraula amb la qual reemplaçar.
	 * @param prefix     Prefix per a la visualització.
	 * @param subPrefix  Prefix addicional per a subdirectoris.
	 */
	private void buscarYReemplaçarParaulaEnArbre(File directori, String paraula, String reemplaçar, String prefix,
			String subPrefix) {
		File[] fitxers = directori.listFiles();

		if (fitxers != null && fitxers.length > 0) {
			for (int i = 0; i < fitxers.length; i++) {
				File fitxer = fitxers[i];
				boolean esUltim = (i == fitxers.length - 1);

				if (fitxer.isDirectory()) {
					txtAreaResultats.append(prefix + (esUltim ? "|-- " : "|-- ") + fitxer.getName() + "\n");
					buscarYReemplaçarParaulaEnArbre(fitxer, paraula, reemplaçar,
							subPrefix + (esUltim ? "    " : "|   "), subPrefix + (esUltim ? "    " : "|   "));
				} else {
					String nomFitxer = fitxer.getName();
					String extensio = obtenirExtensio(fitxer);

					if (extensio.equalsIgnoreCase("txt")) {
						reemplaçarParaulesEnFitxer(fitxer, paraula, reemplaçar, prefix + (esUltim ? "|-- " : "|-- "));
					} else {
						txtAreaResultats.append(prefix + fitxer.getName()
								+ " (Fitxer no accessible per reemplaçar, extensió: " + extensio + ")\n");
					}
				}
			}
		}
	}

	/**
	 * Reemplaça les aparicions d'una paraula en un fitxer.
	 * 
	 * @param fitxer     El fitxer en el qual substituir paraules.
	 * @param paraula    La paraula a buscar i reemplaçar.
	 * @param reemplaçar La paraula amb la qual reemplaçar.
	 * @param prefix     Prefix per a la visualització.
	 */
	private void reemplaçarParaulesEnFitxer(File fitxer, String paraula, String reemplaçar, String prefix) {
		int contadorReemplaços = 0;
		StringBuilder contingutNou = new StringBuilder();

		try (BufferedReader br = new BufferedReader(new FileReader(fitxer, StandardCharsets.UTF_8))) {
			String linia;

			while ((linia = br.readLine()) != null) {
				String liniaOriginal = linia;

				if (chkMayusMinus.isSelected()) {
					linia = convertirMinuscules(linia);
					paraula = convertirMinuscules(paraula);
				}

				if (chkAccents.isSelected()) {
					linia = normalitzarSenseAccents(linia);
					paraula = normalitzarSenseAccents(paraula);
				}

				int reemplazosEnLinea = 0;
				while (linia.contains(paraula)) {
					linia = linia.replaceFirst(paraula, reemplaçar);
					reemplazosEnLinea++;
				}

				contingutNou.append(linia).append("\n");
				contadorReemplaços += reemplazosEnLinea;
			}
		} catch (IOException e) {
			txtAreaResultats.append(prefix + "Error al llegir el fitxer: " + fitxer.getName() + "\n");
			return;
		}

		if (contadorReemplaços > 0) {
			File fitxerNou = new File(fitxer.getParent(), "MOD_" + fitxer.getName());
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(fitxerNou, StandardCharsets.UTF_8))) {
				bw.write(contingutNou.toString());
				txtAreaResultats.append(prefix + fitxer.getName() + " (" + contadorReemplaços
						+ " reemplaços, fitxer nou: " + fitxerNou.getName() + ")\n");
			} catch (IOException e) {
				txtAreaResultats.append(prefix + "Error al escriure el nou fitxer: " + fitxerNou.getName() + "\n");
			}
		} else {
			txtAreaResultats.append(prefix + fitxer.getName() + " (0 reemplaços)\n");
		}
	}

	/**
	 * Create the frame.
	 */
	public Vista() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1139, 780);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(241, 233, 241));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		panel.setBackground(new Color(186, 146, 184));
		panel.setBounds(10, 10, 1105, 145);
		contentPane.add(panel);
		panel.setLayout(null);

		JLabel lblTitol = new JLabel("Gestió de fitxers");
		lblTitol.setBounds(381, 44, 329, 51);
		lblTitol.setFont(new Font("Valken", Font.PLAIN, 40));
		panel.add(lblTitol);

		txtDirectori = new JTextField();
		txtDirectori.setFont(new Font("Trebuchet MS", Font.PLAIN, 20));
		txtDirectori.setBounds(10, 213, 238, 39);
		contentPane.add(txtDirectori);
		txtDirectori.setColumns(10);

		btnDirectori = new JButton("Buscar directori");
		btnDirectori.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				seleccionarDirectori();
				File directoriSeleccionat = new File(txtDirectori.getText());
				txtAreaResultats.setText("");

				if (directoriSeleccionat.exists() && directoriSeleccionat.isDirectory()) {
					llistarArbreFitxers(directoriSeleccionat, "", "");
				} else {
					txtAreaResultats.append("Directori no vàlid.\n");
				}
			}
		});
		btnDirectori.setBackground(new Color(153, 255, 204));
		btnDirectori.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnDirectori.setBounds(284, 212, 185, 39);
		contentPane.add(btnDirectori);

		txtParaula = new JTextField();
		txtParaula.setFont(new Font("Trebuchet MS", Font.PLAIN, 20));
		txtParaula.setColumns(10);
		txtParaula.setBounds(10, 302, 238, 39);
		contentPane.add(txtParaula);

		btnParaula = new JButton("Buscar paraula");
		btnParaula.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String paraula = txtParaula.getText();
				File directoriSeleccionat = new File(txtDirectori.getText());

				if (paraula.isEmpty()) {
					txtAreaResultats.append("Has d'introduir una paraula.\n");
					return;
				}

				if (directoriSeleccionat.exists() && directoriSeleccionat.isDirectory()) {
					txtAreaResultats.setText("");

					if (chkMayusMinus.isSelected()) {
						paraula = convertirMinuscules(paraula);
					}
					if (chkAccents.isSelected()) {
						paraula = normalitzarSenseAccents(paraula);
					}

					buscarParaulaEnArbre(directoriSeleccionat, paraula, "", "");
				} else {
					txtAreaResultats.append("Directori no vàlid.\n");
				}
			}
		});
		btnParaula.setBackground(new Color(153, 153, 255));
		btnParaula.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnParaula.setBounds(284, 301, 185, 39);
		contentPane.add(btnParaula);

		chkMayusMinus = new JCheckBox("Majúscules/Minúscules");
		chkMayusMinus.setFont(new Font("Tahoma", Font.PLAIN, 20));
		chkMayusMinus.setBounds(17, 379, 231, 39);
		contentPane.add(chkMayusMinus);

		txtAreaResultats = new JTextArea();
		txtAreaResultats.setEditable(false);
		txtAreaResultats.setFont(new Font("Trebuchet MS", Font.PLAIN, 14));

		JScrollPane scrollPane = new JScrollPane(txtAreaResultats);
		scrollPane.setBounds(533, 178, 582, 555);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		contentPane.add(scrollPane);

		chkAccents = new JCheckBox("Accents");
		chkAccents.setFont(new Font("Tahoma", Font.PLAIN, 20));
		chkAccents.setBounds(19, 432, 106, 39);
		contentPane.add(chkAccents);

		txtReemplaçar = new JTextField();
		txtReemplaçar.setFont(new Font("Trebuchet MS", Font.PLAIN, 20));
		txtReemplaçar.setColumns(10);
		txtReemplaçar.setBounds(10, 540, 185, 39);
		contentPane.add(txtReemplaçar);

		btnReemplaçar = new JButton("Reemplaçar paraula");
		btnReemplaçar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtAreaResultats.setText("");
				String directori = txtDirectori.getText();
				String paraula = txtParaula.getText();
				String reemplaçar = txtReemplaçar.getText();

				if (!directori.isEmpty() && !paraula.isEmpty() && !reemplaçar.isEmpty()) {
					File fitxerDirectori = new File(directori);
					buscarYReemplaçarParaulaEnArbre(fitxerDirectori, paraula, reemplaçar, "", "");
				} else {
					txtAreaResultats.append("Introdu un directori, una paraula i un reemplaçament.\n");
				}
			}
		});
		btnReemplaçar.setBackground(new Color(255, 153, 102));
		btnReemplaçar.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnReemplaçar.setBounds(231, 539, 238, 39);
		contentPane.add(btnReemplaçar);
	}
}
