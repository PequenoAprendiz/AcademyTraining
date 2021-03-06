package wereh.academytraining.apresentacao.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.List;

import wereh.academytraining.R;
import wereh.academytraining.apresentacao.AdicionarPlanejamento;
import wereh.academytraining.apresentacao.DadosPlanejamentoActivity;
import wereh.academytraining.apresentacao.ExerciciosListaActivity;
import wereh.academytraining.apresentacao.HomeActivity;
import wereh.academytraining.apresentacao.adpters.PlanejamentoAdapter;
import wereh.academytraining.entidade.Planejamento;
import wereh.academytraining.exceptions.DependenciaDeTreinoException;
import wereh.academytraining.exceptions.ExercicioNaoCadastradoPeloUsuario;
import wereh.academytraining.negocio.ExercicioBo;
import wereh.academytraining.negocio.PlanejamentoBo;
import wereh.academytraining.persistencia.DatabaseHelper;
import wereh.academytraining.persistencia.PlanejamentoDao;

import static wereh.academytraining.R.id.listViewFichaDeTreino;

public class FragmentActivityPlanejamentos extends Fragment {

    /// listagem de todos os planejamentos  ou a ficha completa de treino de um determinado período

    ListView mListView;
    public List<Planejamento> listaPlanejamentos;
    private DatabaseHelper dh;
    private PlanejamentoDao planejamentoDao;


    public FragmentActivityPlanejamentos() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.dh = new DatabaseHelper(getContext());
    }

    @Override  // Inflate the layout for this fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_planejamento, container, false);
        this.mListView = (ListView) rootView.findViewById(R.id.listViewFichaDeTreino);
        this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(view.getContext(), DadosPlanejamentoActivity.class);
                intent.putExtra("planejamento", (Parcelable) listaPlanejamentos.get(position));
                startActivity(intent);
            }
        });
        return rootView;
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


    public void carregarLista() throws SQLException {
        this.planejamentoDao = new PlanejamentoDao(dh.getConnectionSource());
        try {
            listaPlanejamentos = this.planejamentoDao.queryForAll();
            this.mListView = (ListView) getActivity().findViewById(listViewFichaDeTreino);
            this.mListView.setAdapter(new PlanejamentoAdapter(getContext(), listaPlanejamentos));
            registerForContextMenu(mListView);                                                   /// registrar a listview no menu de conteexto senão o menus de opções não carrega
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //método para carregar o menu de opçoes no item da listview
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle(listaPlanejamentos.get(info.position).getNomePlanejamento());
        MenuInflater inflater = this.getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_listview_planej, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (getUserVisibleHint()) {
            final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            Fragment fragment = this;
            if (fragment instanceof FragmentActivityPlanejamentos) {
                int id = item.getItemId();

                if (id == R.id.action_Menu_Apagar) {
                    new AlertDialog.Builder(this.getActivity())
                            .setIcon(R.mipmap.ic_delete_black_24dp)
                            .setTitle("Apagando Planejamento")
                            .setMessage("Tem certeza ?")
                            .setPositiveButton("Sim",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            try {
                                                apagarPlanejamento(info);
                                                carregarLista();
                                                Toast.makeText(getActivity(), "Planejamento Apagado!", Toast.LENGTH_SHORT).show();

                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                            } catch (DependenciaDeTreinoException d) {
                                                Toast.makeText(getActivity(), d.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    })
                            .setNegativeButton("Não", null)
                            .show();
                }

                if (id == R.id.action_Menu_Alterar) {
                    Intent i = new Intent(this.getContext(), AdicionarPlanejamento.class);
                    i.putExtra("planejamento", (Parcelable) listaPlanejamentos.get(info.position));
                    startActivity(i);
                }

                if (id == R.id.action_Menu_Detalhes) {
                    Intent intent = new Intent(this.getContext(), DadosPlanejamentoActivity.class);
                    intent.putExtra("planejamento", (Parcelable) listaPlanejamentos.get(info.position));
                    startActivity(intent);
                }

            }
        }
        return super.onOptionsItemSelected(item);
        //return true;
    }


    private void apagarPlanejamento(AdapterView.AdapterContextMenuInfo info) throws SQLException {
        Planejamento planejamento = listaPlanejamentos.get(info.position);
        PlanejamentoBo planejamentoBo = new PlanejamentoBo();
        planejamentoBo.apagarPlanejamento(planejamento, FragmentActivityPlanejamentos.this);
    }
}
