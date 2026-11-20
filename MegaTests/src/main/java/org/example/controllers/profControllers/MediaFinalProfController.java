
package org.example.controllers.profControllers;

import org.example.entity.profEntity.MediaFinalAluno;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.math.RoundingMode;
import java.util.List;

public class MediaFinalProfController {

    @FXML
    private Button btn_voltar_mediafinal_prof;

    @FXML
    private Label lbl_caminho_mediafinal_prof;

    @FXML
    private Label lbl_titulo_mediafinal_prof;

    @FXML
    private VBox vb_mediafinal_prof_container;


    //inicio tela
    @FXML
    public void initialize() {

        lbl_titulo_mediafinal_prof.setText("Médias dos Alunos");

        lbl_caminho_mediafinal_prof.setText(
                "Consulte as médias dos alunos por disciplina"
        );
    }


    //recebe a lista de médias dos alunos
    public void carregarMedias(List<MediaFinalAluno> medias) {

        vb_mediafinal_prof_container.getChildren().clear();

        //verifica se existem registros
        if (medias == null || medias.isEmpty()) {

            Label mensagem = new Label(
                    "Nenhuma média disponível."
            );

            vb_mediafinal_prof_container
                    .getChildren()
                    .add(mensagem);

            return;
        }

        //percorre a lista de alunos
        for (MediaFinalAluno media : medias) {

            VBox cartao = criarCartao(media);

            vb_mediafinal_prof_container
                    .getChildren()
                    .add(cartao);
        }
    }


    //cria o cartão de cada aluno
    private VBox criarCartao(MediaFinalAluno media) {

        VBox cartao = new VBox(10);

        cartao.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-border-color: #dddddd;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-padding: 15px;"
        );


        //nome do aluno
        Label nomeAluno = new Label(
                media.getNomeAluno()
        );


        //RA do aluno
        Label raAluno = new Label(
                "RA: " + media.getRaAluno()
        );


        //nome
        Label disciplina = new Label(
                "Disciplina: " + media.getNomeDisciplina()
        );


        //média final
        String valorMedia = "Não disponível";

        if (media.getMediaFinal() != null) {

            valorMedia = media.getMediaFinal()
                    .setScale(2, RoundingMode.HALF_UP)
                    .toPlainString()
                    .replace(".", ",");
        }

        Label mediaFinal = new Label(
                "Média final: " + valorMedia
        );


        //adiciona os elementos ao cartão
        cartao.getChildren().addAll(
                nomeAluno,
                raAluno,
                disciplina,
                mediaFinal
        );

        return cartao;
    }


    //botão voltar
    @FXML
    void voltar(ActionEvent event) {
        //pfv implementar o evento de voltar para tela anterior

    }

}
