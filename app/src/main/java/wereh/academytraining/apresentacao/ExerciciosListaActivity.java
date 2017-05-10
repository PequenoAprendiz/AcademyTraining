package wereh.academytraining.apresentacao;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import wereh.academytraining.R;
import wereh.academytraining.entidade.Exercicio;
import wereh.academytraining.entidade.GrupoMuscular;
import wereh.academytraining.persistencia.DatabaseHelper;
import wereh.academytraining.persistencia.ExercicioDao;
import wereh.academytraining.persistencia.GrupoMuscularDao;

public class ExerciciosListaActivity extends AppCompatActivity {

    public static final int CODIGO_ACTITIVITY_ADICIONAR_TREINO = 2;
    private List<Exercicio> exercicios;
    private List<GrupoMuscular> gruposMusculares;
    List<Exercicio> exerciciosDoGrupoSelecionado;
    private ListView mListView;
    private DatabaseHelper dh;
    private ExercicioDao exercicioDao;
    private GrupoMuscularDao gmDao;
    private Exercicio exercicio;
    private int idSelected;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercicios_lista);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarListaExercicios);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        cadastrarExercicios();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final GrupoMuscular gmFinal = (GrupoMuscular) getIntent().getSerializableExtra("GrupoMusculares");

        getSupportActionBar().setTitle(gmFinal.getNomeGrupoMuscular());

        if(gmFinal.getNomeGrupoMuscular().equals("Peitoral")){
            getSupportActionBar().setLogo(R.drawable.ic_peitoral);
        }else if (gmFinal.getNomeGrupoMuscular().equals("Bíceps")){
            getSupportActionBar().setLogo(R.drawable.ic_biceps);
        }

        idSelected = gmFinal.getId();
        // positionSelected = getIntent().getIntExtra("POSITION", 0);

        mListView = (ListView) findViewById(R.id.listViewExercicios);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent();
                intent.putExtra("exercicio", (Parcelable) exerciciosDoGrupoSelecionado.get(position));
                setResult(CODIGO_ACTITIVITY_ADICIONAR_TREINO,intent);
                finish();

            }
        });
    }

    @Override
    public void onResume() {
        try {
            super.onResume();
            this.carregarLista();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  void carregarLista() throws SQLException {
        // ArrayAdapter<Exercicio> adapter;
        //int adapterLayout = android.R.layout.simple_list_item_1;
        exercicioDao  = new ExercicioDao(dh.getConnectionSource());

        try {


            QueryBuilder<Exercicio, Integer> queryBuilder = exercicioDao.queryBuilder();

            queryBuilder.where().eq(Exercicio.IDGRUPOMUSCULAR_FIELD_NAME, idSelected);

            PreparedQuery<Exercicio> preparedQuery = queryBuilder.prepare();

            exerciciosDoGrupoSelecionado = exercicioDao.query(preparedQuery);

            //adapter = new ArrayAdapter<Exercicio>(this, adapterLayout, exerciciosDoGrupoSelecionado);

            mListView = (ListView) findViewById(R.id.listViewExercicios);
            //this.mListView.setAdapter(adapter);
            mListView.setAdapter( new ExerciciosAdapter(this, exerciciosDoGrupoSelecionado));
            registerForContextMenu(mListView);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //método para carregar o menu de opçoes no item da listview
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;

        menu.setHeaderTitle(exerciciosDoGrupoSelecionado.get(info.position).getNomeExercicio());

        MenuInflater inflater = this.getMenuInflater();

        inflater.inflate(R.menu.menu_listview, menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        int id = item.getItemId();

//        if (id == R.id.action_Menu_Apagar) {
//            ManobrasDao m = new ManobrasDao(this.getContext());
//            Manobra manobra = listaManobras.get(info.position);
//            // Log.e("QTD",listaManobras.get(info.position).getId()+"");
//            m.deletar(manobra.getId());
//            this.carregarLista();
//        }

//        if (id == R.id.action_Menu_Alterar) {
//            Intent i = new Intent(this.getContext(), Alterar.class);
//            i.putExtra("manobra", (Parcelable) listaManobras.get(info.position));
//            startActivity(i);
//        }

        if (id == R.id.action_Menu_Detalhes) {
            Intent intent = new Intent(this, ExercicioDetalhesActivity.class);
            intent.putExtra("exercicio", (Parcelable) exerciciosDoGrupoSelecionado.get(info.position));
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
        //return true;
    }

    private void cadastrarExercicios() {
        try {
            dh = new DatabaseHelper(ExerciciosListaActivity.this);
            gmDao = new GrupoMuscularDao(dh.getConnectionSource());
            exercicioDao = new ExercicioDao(dh.getConnectionSource());
            exercicios = new ArrayList<Exercicio>();
            gruposMusculares = new ArrayList<GrupoMuscular>();
            gruposMusculares = gmDao.queryForAll();


            for (GrupoMuscular gm : gruposMusculares) {
                if(gm.getNomeGrupoMuscular().equals("Peitoral")){

                    exercicio = new Exercicio();
                    exercicio.setNomeExercicio("Supino Reto");
                    exercicio.setGrupoMuscular(gm.getId());
                    exercicio.setDescricao("Músculos envolvidos: Peitoral, tríceps e deltoide\n" +
                            "\n" +
                            "Equipamentos: Barra, Smith ou halteres"+
                            "O supino reto é o principal e mais básico exercício para peitoral e um dos três grandes da musculação. Ele configura-se por um exercício que recruta muitas fibras musculares e vários músculos auxiliares. Todavia, a perfeita forma de execução do exercício é necessária, pois, ele é muito propenso de lesionar o ombro, pela angulação em que se encontra e pelo tipo de movimento. Ainda, a posição dos braços deve ser devidamente observada para que não haja problemas relacionados ao ombro, especialmente.\n" +
                            "\n"+"O supino reto pode ser executado tanto com barra quanto com halteres. O Smith machine (barra guiada) também é uma boa opção por conferir uma boa estabilidade e segurança no movimento.");
                    exercicios.add(exercicio);

                    exercicio = new Exercicio();
                    exercicio.setNomeExercicio("Supino inclinado");
                    exercicio.setGrupoMuscular(gm.getId());
                    exercicio.setDescricao("Músculos envolvidos: Peitoral, Deltoides, tríceps\n\n" +
                            "\n" +
                            "Equipamentos: Barra ou halteres"+
                            "Próximo ao supino reto, a diferença marcante que temos no supino inclinado é o trabalho mais específico para a região superior do peitoral. Todavia, um cuidado extra deve ser tomado nesse exercício que é manter a região lombar sempre devidamente apoiada no banco. A maioria dos indivíduos, por utilizar altas quantidades de carga, peca nesse aspecto.\n" +
                            "\n" + "Esse exercício pode ser realizado em angulações de 45º e 30º, sendo ainda possível fazê-lo com barra ou com halteres. O Smith machine (narra guiada) também é uma boa opção por conferir uma boa estabilidade e segurança no movimento, além de menor necessidade de equilíbrio em situações de fadiga (por exemplo, quando esse exercício é colocado ao final do treino).");
                    exercicios.add(exercicio);

                    exercicio = new Exercicio();
                    exercicio.setNomeExercicio("Pullover");
                    exercicio.setGrupoMuscular(gm.getId());
                    exercicio.setDescricao("Músculos envolvidos: Tríceps, peitoral, dorsal, serrátil anterior e deltoides\n" +
                            "\n" +
                            "Equipamentos: Halter, barra ou cabos\n" +
                            "\n" +
                            "Sendo possível de se executar com os três equipamentos supracitados, o pullover, quando realizado para peitoral e não para dorsais requer uma angulação mais fechada dos cotovelos e uma fase excêntrica menos extensiva, a fim de concentrar a força especificamente na região do peitoral, solicitando menos os dorsais e mesmo o serrátil anteior.\n" +
                            "\n" +
                            "O pullover é um exercício que recruta, especialmente aparte lateral e inferior do peitoral, local de difícil foco para a maioria dos outros exercícios. Ele também auxilia a trabalhar a largura dos peitorais.");
                    exercicios.add(exercicio);

                    exercicio = new Exercicio();
                    exercicio.setNomeExercicio("PeckDeck");
                    exercicio.setGrupoMuscular(gm.getId());
                    exercicio.setDescricao("Músculos envolvidos: Peitoral\n" +
                            "\n" +
                            "Equipamentos: Peck-Deck\n" +
                            "\n" +
                            "O Peck-Deck era um exercício bem mais observado nas academias antigas, mas, hoje parece que elas tem optado pelo Flye Machine, talvez pela maior versatilidade de poder usá-lo para o trabalho de deltoides posteriores e trapézio também. Todavia, esse é um grande exercício o qual isola por completo os peitorais e é uma ótima opção de pré-exaustão, como costumava fazer Mike Mentzer, antes do supino inclinado.\n" +
                            "\n" +
                            "O peck-deck deve sempre ser valorizado em sua fase excêntrica, promovendo uma completa extensão do peitoral Já na fase concêntrica, não há necessidade de parar em isometria na contração máxima, pois, isso aliviará a tensão causada durante o exercício e, portanto irá diminuir sua eficácia.\n" +
                            "\n" +
                            "Promova nesse exercício movimentos sempre muito bem concentrados e, se possível, sempre lentos também.");
                    exercicios.add(exercicio);

                    exercicio = new Exercicio();
                    exercicio.setNomeExercicio("Press com cabos");
                    exercicio.setGrupoMuscular(gm.getId());
                    exercicio.setDescricao("Músculos envolvidos: Peitoral, deltoides e tríceps\n" +
                            "\n" +
                            "Equipamentos: Cabos\n" +
                            "\n" +
                            "Muitos utilizam os cabos no treinamento de peitorais apenas para flyes, cross over entre outros movimentos. Porém, o press com cabos é um poderoso exercício o qual pode ser utilizado de maneira auxiliar no seu treinamento. Ele basicamente simula um supino na máquina, mas, ao invés dos pegadores na mesma, você utiliza cabos, em uma polia ajustável, claro. Com a utilização dos cabos, você tenderá a deixar os braços cair, portanto, esse exercício recrutará muito das suas forças auxiliares e de sua capacidade neuromuscular para o equilíbrio e estabilidade do movimento.");
                    exercicios.add(exercicio);

                    exercicio = new Exercicio();
                    exercicio.setNomeExercicio("Cross over");
                    exercicio.setGrupoMuscular(gm.getId());
                    exercicio.setDescricao("Músculos envolvidos: Peitoral\n" +
                            "\n" +
                            "Equipamentos: Cabos\n" +
                            "\n" +
                            "O cross over é talvez o exercício com cabos para peitoral mais típico. Ele é um movimento especialmente para o peitoral menor, na sua variação principal. Todavia, esse movimento também pode ser para o peitoral maior ou mesmo para o peitoral como um todo, dependendo da angulação que ele é executado.\n" +
                            "\n" +
                            "É sempre importante atentar-se à forma desse exercício, pois, são muito comuns lesões de peitoral menor e, principalmente de manguito rotador, por ser um conjunto de músculos que estabilizam o úmero nesse movimento.");
                    exercicios.add(exercicio);

                    exercicio = new Exercicio();
                    exercicio.setNomeExercicio("Supino declinado");
                    exercicio.setGrupoMuscular(gm.getId());
                    exercicio.setDescricao("Músculos envolvidos: Peitoral, tríceps, ombro\n" +
                            "\n" +
                            "Equipamentos: Smith, Barra ou halteres\n" +
                            "\n" +
                            "O supino declinado é um exercício que valoriza maiormente o peitoral menor. Assim como os outros supinos, ele pode ser executado tanto com barra quanto com halteres, a depender do enfoque que você queira dar. Ele é um movimento que facilmente pode lesionar o manguito rotador, portanto, a sua boa execução é primordial e não deve ser negligenciada jamais.\n" +
                            "\n" +
                            "Esse é um movimento que pode ser realizado com barra, halter ou mesmo no Smith, porém, o Smith é um pouco menos utilizado pelo fato de que não existem muitos bancos que possam ser facilmente transportados ao equipamento em questão.");
                    exercicios.add(exercicio);

                    exercicio = new Exercicio();
                    exercicio.setNomeExercicio("Crucifixo declinado");
                    exercicio.setGrupoMuscular(gm.getId());
                    exercicio.setDescricao("úsculos envolvidos: Peitoral e deltoides\n" +
                            "\n" +
                            "Equipamentos: Halteres\n" +
                            "\n" +
                            "O Crucifixo declinado também é muito pouco visto na maioria das academias de musculação. Ele também visa um trabalho no peitoral inferior e é um ótimo exercício para a delineação na parte inferior do peitoral, desde o externo até a parte lateral do corpo.\n" +
                            "\n" +
                            "Esse movimento pode causar grande instabilidade na articulação glenoumeral, portanto, é fundamental que se execute o movimento com uma boa estabilidade e com controle do movimento, do contrário, a chance de lesão é muito alta.");
                    exercicios.add(exercicio);


                    exercicio = new Exercicio();
                    exercicio.setNomeExercicio("Mergulho (barras paralelas)");
                    exercicio.setGrupoMuscular(gm.getId());
                    exercicio.setDescricao("Músculos envolvidos: Peitoral, deltoides e deltoides\n" +
                            "\n" +
                            "Equipamentos: Paralelas e gravitron\n" +
                            "\n" +
                            "As barras paralelas permitem a realização do mergulho que é um exercício ímpar para quem deseja um corpo realmente diferente! Sendo um exercício multiarticular, composto e, principalmente com um poder de recrutamento muscular excepcional, o mergulho é um exercício que auxiliara no treinamento do peitoral inferior, mais especificamente, mas ainda, recrutará a porção frontal dos deltoides e, principalmente, os tríceps. Aliás, ele é um exercício muito utilizado para tríceps.\n" +
                            "\n" +
                            "As barras paralelas tem a capacidade de serem versáteis, por isso, caso o seu enfoque sejam os peitorais inferiores, curve-se um pouco mais barra frente, fazendo uma leve flexão dos ombros. Porém, se o seu objetivo for o trabalho de tríceps, o ideal é manter-se reto, perpendicular ao solo.\n" +
                            "\n" +
                            "As barras paralelas apesar de serem ótimas, necessitam de cuidados, principalmente com os ombros. Isso porque, o peso de todo corpo (isso, quando não adicionamos peso ao corpo) exige uma enorme força do manguito rotador, pois, a tendência é de que o úmero se empurrado para cima, promovendo, por exemplo, quadros de síndrome do impacto. Portanto, atenção sempre!\n" +
                            "\n" +
                            "Para indivíduos com lesões, com algum tipo de necessidade específica ou muito pesados, talvez o gravitron possa ser uma boa opção. Ele auxiliará também iniciantes e, a maioria das mulheres.");
                    exercicios.add(exercicio);


                    exercicio = new Exercicio();
                    exercicio.setNomeExercicio("Supinos com pegadas neutras");
                    exercicio.setGrupoMuscular(gm.getId());
                    exercicio.setDescricao("Músculos envolvidos: Peitoral, tríceps e deltoides\n" +
                            "\n" +
                            "Equipamentos: Barras, halteres, máquinas\n" +
                            "\n" +
                            "Os supinos com pegadas neutras, geralmente solicitam a porção lateral do peitoral e são ótimo exercícios (a depender da angulação) para trabalharmos a região inferior do peitoral (por exemplo, com a angulação reta). Dessa forma, eles podem ser utilizados com barras (existem barras em H, pouco vistas nas academias com o tamanho da barra de supino) e com halteres e máquinas, que são as formas mais comuns de realizar o movimento pela facilidade de aderência e mesmo segurança.\n" +
                            "\n" +
                            "Esse não é um exercício básico e que vai te trazer ganhos incríveis de massa muscular, mas, certamente será uma ótima ferramenta para trabalhos específicos e para a melhoria de algumas regiões.");
                    exercicios.add(exercicio);

                    exercicio = new Exercicio();
                    exercicio.setNomeExercicio("Crucifixo reto");
                    exercicio.setGrupoMuscular(gm.getId());
                    exercicio.setDescricao("Músculos envolvidos: Peitoral e deltoides\n" +
                            "\n" +
                            "Equipamentos: Halteres, cabos, máquina\n" +
                            "\n" +
                            "O crucifixo reto é um dos movimentos fundamentais para o peitoral. Trabalhando-o por completo, é um movimento de fácil realização, que não requer grandes trabalhos para a aderência do movimento e que recruta um altíssimo grau de fibras musculares. O crucifixo reto ainda, pode ser realizado em com halteres, que é o modo mais comum, com máquinas (fly machine) ou com cabos, dando uma tensão contínua ao movimento e não deixando que seu peitoral entre em algum tipo de relaxamento. Ele também pode conferir mais segurança a depender do caso.");
                    exercicios.add(exercicio);

                    exercicio = new Exercicio();
                    exercicio.setNomeExercicio("Crucifixo inclinado");
                    exercicio.setGrupoMuscular(gm.getId());
                    exercicio.setDescricao("Músculos envolvidos: Peitoral e deltoides\n" +
                            "\n" +
                            "Equipamentos: Halteres e cabos\n" +
                            "\n" +
                            "Da mesma forma que o crucifixo reto, o inclinado não se altera em muita coisa. Porém, pela angulação do banco, você acaba recrutando maiormente o peitoral superior, sendo que, a angulação ideal para esse movimento é a de 30º e não a de 45º . Isso se deve ao fato de que a 30º conseguimos uma melhor fase excêntrica do movimento com uma maior extensão do peitoral e sem flexionar demais os cotovelos, o que acaba tirando um pouco da tensão do peitoral maior.");
                    exercicios.add(exercicio);

                    exercicio = new Exercicio();
                    exercicio.setNomeExercicio("Press inclinado com cabos");
                    exercicio.setGrupoMuscular(gm.getId());
                    exercicio.setDescricao("Músculos envolvidos: Peitoral, tríceps e deltoides\n" +
                            "\n" +
                            "Equipamentos: Cabos\n" +
                            "\n" +
                            "Mencionamos anteriormente o supino reto com cabos. O mesmo pode ser feito na versão inclinada, o que recrutará de melhor forma o peitoral na região superior. Além disso, os deltoides frontais também serão mais solicitados no movimento.");
                    exercicios.add(exercicio);

                    exercicio = new Exercicio();
                    exercicio.setNomeExercicio("Flexões");
                    exercicio.setGrupoMuscular(gm.getId());
                    exercicio.setDescricao("Músculos envolvidos: Peitoral, tríceps e deltoides\n" +
                            "\n" +
                            "Equipamentos: Próprio corpo\n" +
                            "\n" +
                            "Muito utilizado em exercícios de calitenia, por exemplo, os exercícios que envolvem flexões são ótimos para quem quer deixar os pesos um pouco de lado ou tentar uma variação diferente. Eles podem fazer com que você comece a saber utilizar a carga do seu próprio corpo para obter resultados.\n" +
                            "\n" +
                            "Eles podem ser executados em inúmeras angulações a cada qual conferirá um trabalho específico em cada região do peitoral (superior, média e inferior).\n" +
                            "\n" +
                            "Obviamente, você não deve esperar ganhos exorbitantes de massa muscular, mas, poderá esperar fatores como resistência, equilíbrio etc.");
                    exercicios.add(exercicio);

                    exercicio = new Exercicio();
                    exercicio.setNomeExercicio("Press em máquinas articuladas");
                    exercicio.setGrupoMuscular(gm.getId());
                    exercicio.setDescricao("Músculos envolvidos: Peitoral, tríceps e deltoides\n" +
                            "\n" +
                            "Equipamentos: Máquinas\n" +
                            "\n" +
                            "Podendo ser em máquinas que simulam o supino declinado, o supino inclinado ou mesmo o supino reto, os exercícios em máquinas são excelentes por conferirem isolamento, segurança e atingirem exatamente onde você quer. São também exercícios ideais para serem utilizados em momentos onde a instabilidade dos músculos auxiliares e/ou estabilizadores está desgastada.\n" +
                            "\n" +
                            "Ainda, a depender da pessoa que estamos falando, são ótimos para auxiliar em lesões e reabilitações das mesmas, sendo assim uma importante ferramenta da fisioterapia.\n" +
                            "\n" +
                            "Existem inúmeras marcas desses equipamentos, porém, entre as mais antigas está a Hammer Strenght que confere uma amplitude muito peculiar aos movimentos.");
                    exercicios.add(exercicio);

//                    exercicio = new Exercicio();
//                    exercicio.setNomeExercicio("PeckDeck Peitoral");
//                    exercicio.setGrupoMuscular(gm.getId());
//                    exercicio.setDescricao("Músculos envolvidos: Peitoral\n" +
//                            "\n" +
//                            "Equipamentos: Peck-Deck");
//                    exercicios.add(exercicio);




































                }

                if(gm.getNomeGrupoMuscular().equals("Bíceps")){
                    exercicio = new Exercicio();
                    exercicio.setNomeExercicio("Rosca Concentrada");
                    exercicio.setGrupoMuscular(gm.getId());
                    exercicios.add(exercicio);

                    exercicio = new Exercicio();
                    exercicio.setNomeExercicio("Bíceps 21");
                    exercicio.setGrupoMuscular(gm.getId());
                    exercicios.add(exercicio);

                                    }
            }

            for (Exercicio ex : exercicios) {
                exercicioDao.create(ex);
            }
            exercicios = null;


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}