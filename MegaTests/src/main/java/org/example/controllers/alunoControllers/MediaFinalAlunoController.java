
package org.example.controllers.alunoControllers;

import org.example.entity.alunoEntity.MediaFinal;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.math.RoundingMode;
import java.util.List;

public class MediaFinalAlunoController {

    @FXML
    private Button btn_voltar_mediafinal_aluno;

    @FXML
    private Label lbl_caminho_mediafinal_aluno;

    @FXML
    private Label lbl_titulo_mediafinal_aluno;

    @FXML
    private VBox vb_mediafinal_aluno_container;


    //inicio da tela
    @FXML
    public void initialize() {

        lbl_titulo_mediafinal_aluno.setText("Média Final");

        lbl_caminho_mediafinal_aluno.setText(
                "Confira suas médias por disciplina"
        );
    }


    //recebe as medias
    public void carregarMedias(List<MediaFinal> medias) {

        vb_mediafinal_aluno_container.getChildren().clear();

        //tem média?
        if (medias == null || medias.isEmpty()) {

            Label mensagem = new Label(
                    "Nenhuma média disponível."
            );

            vb_mediafinal_aluno_container
                    .getChildren()
                    .add(mensagem);

            return;
        }

        //percorre a lista de médias
        for (MediaFinal media : medias) {

            VBox cartao = criarCartao(media);

            vb_mediafinal_aluno_container
                    .getChildren()
                    .add(cartao);
        }
    }


    //cria o cartão de cada disciplina
    private VBox criarCartao(MediaFinal media) {

        VBox cartao = new VBox(10);

        cartao.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-border-color: #dddddd;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-padding: 15px;"
        );


        //nome
        Label titulo = new Label(
                media.getNomeDisciplina()
        );


        //quantidade de avaliações
        Label provas = new Label(
                media.getQuantidadeprova() + " provas consideradas"
        );


        //média final
        String valorMedia = "Não disponível";

        if (media.getMediafinal() != null) {

            valorMedia = media.getMediafinal()
                    .setScale(2, RoundingMode.HALF_UP)
                    .toPlainString()
                    .replace(".", ",");
        }

        Label mediaFinal = new Label(
                "Média final: " + valorMedia
        );


        //adiciona os elementos ao cartão
        cartao.getChildren().addAll(
                titulo,
                provas,
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
