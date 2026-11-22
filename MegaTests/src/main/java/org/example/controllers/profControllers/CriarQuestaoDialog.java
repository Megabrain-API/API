package org.example.controllers.profControllers;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.example.dao.DisciplinaDAO;
import org.example.dao.QuestaoDAO;
import org.example.entity.profEntity.Questao;

import java.util.ArrayList;
import java.util.List;

public class CriarQuestaoDialog {
    private CriarQuestaoDialog() {}

    static void abrir(Window owner) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Nova Questão - Banco de Questões");

        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.setPrefWidth(680);
        root.setStyle("-fx-background-color: #F4F7F5;");

        Label lblHeader = new Label("Cadastrar Nova Questão no Banco");
        lblHeader.setFont(Font.font("System", FontWeight.BOLD, 18));
        lblHeader.setStyle("-fx-text-fill: #1B1B1B;");

        Label lblSub = new Label("A questão será salva sem pontuação fixa; o peso será definido ao montar a prova.");
        lblSub.setStyle("-fx-text-fill: #111111; -fx-font-size: 12px;");

        HBox linhaDisciplinaTipo = new HBox(12);
        VBox colDisc = new VBox(4);
        HBox.setHgrow(colDisc, Priority.ALWAYS);
        Label lblDisc = new Label("Disciplina:");
        lblDisc.setStyle("-fx-text-fill: #111111; -fx-font-weight: bold; -fx-font-size: 12px;");
        ComboBox<String> cmbDisciplina = new ComboBox<>();
        cmbDisciplina.setItems(FXCollections.observableArrayList(new DisciplinaDAO().listarNomesDisciplinas()));
        cmbDisciplina.setMaxWidth(Double.MAX_VALUE);
        colDisc.getChildren().addAll(lblDisc, cmbDisciplina);

        VBox colTipo = new VBox(4);
        HBox.setHgrow(colTipo, Priority.ALWAYS);
        Label lblTipo = new Label("Tipo de questão:");
        lblTipo.setStyle("-fx-text-fill: #111111; -fx-font-weight: bold; -fx-font-size: 12px;");
        ComboBox<String> cmbTipo = new ComboBox<>(FXCollections.observableArrayList(
                "Múltipla escolha",
                "Checklist numerado",
                "Dissertativa",
                "Correspondência"
        ));
        cmbTipo.setValue("Múltipla escolha");
        cmbTipo.setMaxWidth(Double.MAX_VALUE);
        colTipo.getChildren().addAll(lblTipo, cmbTipo);
        linhaDisciplinaTipo.getChildren().addAll(colDisc, colTipo);

        VBox colEnunciado = new VBox(4);
        Label lblEnun = new Label("Enunciado da questão:");
        lblEnun.setStyle("-fx-text-fill: #111111; -fx-font-weight: bold; -fx-font-size: 12px;");
        TextArea txtEnunciado = new TextArea();
        txtEnunciado.setPromptText("Digite o enunciado completo da questão...");
        txtEnunciado.setWrapText(true);
        txtEnunciado.setPrefRowCount(3);
        colEnunciado.getChildren().addAll(lblEnun, txtEnunciado);

        // Área Objetiva / Checklist
        VBox areaAlternativasBox = new VBox(8);
        Label lblAlts = new Label("Alternativas (marque a(s) correta(s)):");
        lblAlts.setStyle("-fx-text-fill: #111111; -fx-font-weight: bold; -fx-font-size: 12px;");
        VBox listaAlternativas = new VBox(6);
        List<TextField> camposAlt = new ArrayList<>();
        List<CheckBox> checksAlt = new ArrayList<>();

        Runnable addAlt = () -> {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            TextField tf = new TextField();
            tf.setPromptText("Texto da alternativa...");
            HBox.setHgrow(tf, Priority.ALWAYS);
            CheckBox chk = new CheckBox("Correta");
            chk.setStyle("-fx-text-fill: #285A36; -fx-font-weight: bold;");
            Button btnRem = new Button("Remover");
            btnRem.setOnAction(ev -> {
                listaAlternativas.getChildren().remove(row);
                camposAlt.remove(tf);
                checksAlt.remove(chk);
            });
            camposAlt.add(tf);
            checksAlt.add(chk);
            row.getChildren().addAll(tf, chk, btnRem);
            listaAlternativas.getChildren().add(row);
        };
        addAlt.run();
        addAlt.run();

        Button btnAddAlt = new Button("+ Adicionar alternativa");
        btnAddAlt.setStyle("-fx-background-color: #EEF5F0; -fx-border-color: #C4D6CA; -fx-text-fill: #285A36; -fx-font-weight: bold;");
        btnAddAlt.setOnAction(ev -> addAlt.run());
        areaAlternativasBox.getChildren().addAll(lblAlts, listaAlternativas, btnAddAlt);

        // Área Dissertativa
        VBox areaDissertativaBox = new VBox(4);
        Label lblMod = new Label("Resposta de referência / gabarito (opcional):");
        lblMod.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        TextArea txtModelo = new TextArea();
        txtModelo.setPromptText("Digite a resposta esperada...");
        txtModelo.setWrapText(true);
        txtModelo.setPrefRowCount(3);
        areaDissertativaBox.getChildren().addAll(lblMod, txtModelo);

        // Área Correspondência
        VBox areaCorrBox = new VBox(8);
        Label lblItensC = new Label("Coluna 1 — Itens numerados:");
        lblItensC.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        VBox listaItensC = new VBox(6);
        List<TextField> camposItensC = new ArrayList<>();

        Label lblOpcoesC = new Label("Coluna 2 — Opções e item correspondente:");
        lblOpcoesC.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        VBox listaOpcoesC = new VBox(6);
        List<TextField> camposOpcoesC = new ArrayList<>();
        List<ComboBox<Integer>> combosOpcoesC = new ArrayList<>();

        Runnable syncCombos = () -> {
            int n = camposItensC.size();
            for (ComboBox<Integer> cb : combosOpcoesC) {
                Integer prev = cb.getValue();
                cb.getItems().clear();
                for (int i = 1; i <= n; i++) cb.getItems().add(i);
                if (prev != null && prev <= n) cb.setValue(prev);
            }
        };

        Runnable addItemC = () -> {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            Label lblN = new Label((camposItensC.size() + 1) + ".");
            TextField tf = new TextField();
            tf.setPromptText("Item...");
            HBox.setHgrow(tf, Priority.ALWAYS);
            Button btnRem = new Button("Remover");
            btnRem.setOnAction(ev -> {
                listaItensC.getChildren().remove(row);
                camposItensC.remove(tf);
                for (int i = 0; i < listaItensC.getChildren().size(); i++) {
                    HBox r = (HBox) listaItensC.getChildren().get(i);
                    ((Label) r.getChildren().get(0)).setText((i + 1) + ".");
                }
                syncCombos.run();
            });
            camposItensC.add(tf);
            row.getChildren().addAll(lblN, tf, btnRem);
            listaItensC.getChildren().add(row);
            syncCombos.run();
        };

        Runnable addOpcaoC = () -> {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            ComboBox<Integer> cbNum = new ComboBox<>();
            cbNum.setPromptText("Item");
            cbNum.setPrefWidth(80);
            TextField tf = new TextField();
            tf.setPromptText("Opção correspondente...");
            HBox.setHgrow(tf, Priority.ALWAYS);
            Button btnRem = new Button("Remover");
            btnRem.setOnAction(ev -> {
                listaOpcoesC.getChildren().remove(row);
                camposOpcoesC.remove(tf);
                combosOpcoesC.remove(cbNum);
            });
            camposOpcoesC.add(tf);
            combosOpcoesC.add(cbNum);
            row.getChildren().addAll(cbNum, tf, btnRem);
            listaOpcoesC.getChildren().add(row);
            syncCombos.run();
        };
        addItemC.run();
        addItemC.run();
        addOpcaoC.run();
        addOpcaoC.run();

        Button btnAddItemC = new Button("+ Adicionar item");
        btnAddItemC.setOnAction(ev -> addItemC.run());
        Button btnAddOpcaoC = new Button("+ Adicionar opção");
        btnAddOpcaoC.setOnAction(ev -> addOpcaoC.run());
        areaCorrBox.getChildren().addAll(lblItensC, listaItensC, btnAddItemC, lblOpcoesC, listaOpcoesC, btnAddOpcaoC);

        Runnable atualizarVisibilidade = () -> {
            String t = cmbTipo.getValue();
            boolean diss = "Dissertativa".equals(t);
            boolean corr = "Correspondência".equals(t);
            boolean obj = !diss && !corr;

            areaAlternativasBox.setVisible(obj);
            areaAlternativasBox.setManaged(obj);
            areaDissertativaBox.setVisible(diss);
            areaDissertativaBox.setManaged(diss);
            areaCorrBox.setVisible(corr);
            areaCorrBox.setManaged(corr);
        };
        cmbTipo.setOnAction(ev -> atualizarVisibilidade.run());
        atualizarVisibilidade.run();

        ScrollPane scroll = new ScrollPane(new VBox(12, linhaDisciplinaTipo, colEnunciado, areaAlternativasBox, areaDissertativaBox, areaCorrBox));
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        HBox botoesModal = new HBox(10);
        botoesModal.setAlignment(Pos.CENTER_RIGHT);
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("-fx-background-color: #FCE8E6; -fx-text-fill: #B3261E; -fx-font-weight: bold; -fx-padding: 8 16 8 16;");
        btnCancelar.setOnAction(ev -> dialog.close());

        Button btnSalvar = new Button("Salvar no Banco");
        btnSalvar.setStyle("-fx-background-color: #4A7C59; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 18 8 18;");
        btnSalvar.setOnAction(ev -> {
            String disc = cmbDisciplina.getValue();
            String enun = txtEnunciado.getText() == null ? "" : txtEnunciado.getText().trim();
            String tipoSel = cmbTipo.getValue();

            if (disc == null || disc.isBlank()) {
                mostrarAlertaErro("Selecione uma disciplina para a questão.");
                return;
            }
            if (enun.isEmpty()) {
                mostrarAlertaErro("Preencha o enunciado da questão.");
                return;
            }

            Questao nova = new Questao();
            nova.setEnunciado(enun);
            nova.setDisciplina(disc);
            nova.setPontos(1.0); // Valor padrão; a nota real é atribuída na prova

            if ("Dissertativa".equals(tipoSel)) {
                nova.setTipo(Questao.DISSERTATIVA);
                nova.setRespostaModelo(txtModelo.getText() == null ? "" : txtModelo.getText().trim());
            } else if ("Correspondência".equals(tipoSel)) {
                nova.setTipo(Questao.CORRESPONDENCIA);
                if (camposItensC.size() < 2 || camposOpcoesC.size() < 2) {
                    mostrarAlertaErro("Correspondência precisa de pelo menos 2 itens e 2 opções.");
                    return;
                }
                for (TextField tf : camposItensC) {
                    if (tf.getText() == null || tf.getText().trim().isEmpty()) {
                        mostrarAlertaErro("Preencha todos os itens de correspondência.");
                        return;
                    }
                    nova.getItensCorrespondencia().add(tf.getText().trim());
                }
                for (int i = 0; i < camposOpcoesC.size(); i++) {
                    String txtOp = camposOpcoesC.get(i).getText() == null ? "" : camposOpcoesC.get(i).getText().trim();
                    Integer num = combosOpcoesC.get(i).getValue();
                    if (txtOp.isEmpty() || num == null) {
                        mostrarAlertaErro("Preencha todas as opções e seus itens correspondentes.");
                        return;
                    }
                    nova.getAlternativasCorrespondencia().add(txtOp);
                    nova.getGabaritoCorrespondencia().add(num);
                }
            } else {
                nova.setTipo("Checklist numerado".equals(tipoSel) ? Questao.CHECKLIST : Questao.MULTIPLA_ESCOLHA);
                if (camposAlt.size() < 2) {
                    mostrarAlertaErro("Adicione pelo menos duas alternativas.");
                    return;
                }
                int corretas = 0;
                for (int i = 0; i < camposAlt.size(); i++) {
                    String txtA = camposAlt.get(i).getText() == null ? "" : camposAlt.get(i).getText().trim();
                    if (txtA.isEmpty()) {
                        mostrarAlertaErro("Preencha o texto de todas as alternativas.");
                        return;
                    }
                    nova.getAlternativas().add(txtA);
                    if (checksAlt.get(i).isSelected()) {
                        nova.getRespostasCorretas().add(i);
                        corretas++;
                    }
                }
                if (corretas == 0) {
                    mostrarAlertaErro("Marque pelo menos uma alternativa correta.");
                    return;
                }
                if (Questao.MULTIPLA_ESCOLHA.equals(nova.getTipo()) && corretas != 1) {
                    mostrarAlertaErro("Múltipla escolha deve ter exatamente uma alternativa correta.");
                    return;
                }
            }

            try {
                new QuestaoDAO().salvarQuestaoAvulsa(nova, disc);
                dialog.close();
            } catch (Exception ex) {
                mostrarAlertaErro("Erro ao salvar questão: " + ex.getMessage());
            }
        });

        botoesModal.getChildren().addAll(btnCancelar, btnSalvar);
        root.getChildren().addAll(lblHeader, lblSub, scroll, botoesModal);

        Scene scene = new Scene(root, 700, 560);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private static void mostrarAlertaErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validação");
        alert.setHeaderText("Não foi possível salvar a questão");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
