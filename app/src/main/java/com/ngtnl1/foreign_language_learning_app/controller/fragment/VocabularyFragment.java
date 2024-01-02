package com.ngtnl1.foreign_language_learning_app.controller.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ngtnl1.foreign_language_learning_app.R;
import com.ngtnl1.foreign_language_learning_app.controller.FillWordActivity;
import com.ngtnl1.foreign_language_learning_app.controller.FlipCardActivity;
import com.ngtnl1.foreign_language_learning_app.controller.QuizzActivity;
import com.ngtnl1.foreign_language_learning_app.controller.adapter.VocabularyAdapter;
import com.ngtnl1.foreign_language_learning_app.databinding.FragmentMainVocabularyBinding;
import com.ngtnl1.foreign_language_learning_app.model.Topic;
import com.ngtnl1.foreign_language_learning_app.model.Vocabulary;
import com.ngtnl1.foreign_language_learning_app.service.AppStatusService;
import com.ngtnl1.foreign_language_learning_app.service.TopicService;
import com.ngtnl1.foreign_language_learning_app.service.UserService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class VocabularyFragment extends Fragment {
    @Inject
    TopicService topicService;
    @Inject
    UserService userService;
    @Inject
    AppStatusService appStatusService;
    Topic topic;
    private FragmentMainVocabularyBinding binding;
    private VocabularyAdapter adapter;
    private RecyclerView recyclerView;
    private List<Vocabulary> items = new ArrayList<>();
    private List<Topic> topics = new ArrayList<>();
    private boolean isLoadByPublic;
    private boolean isLoadByNoted;

    public VocabularyFragment(Topic topic, boolean isLoadByPublic, boolean isLoadByNoted) {
        this.topic = topic;
        this.isLoadByPublic = isLoadByPublic;
        this.isLoadByNoted = isLoadByNoted;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMainVocabularyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();
        setupRecyclerView();
        setOnClickListener();

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setEnabled(false);
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void initViews() {
        recyclerView = binding.recyclerViewMainFragmentVocabulary;

        if (isLoadByPublic || isLoadByNoted) {
            binding.floatingActionButtonMainFragmentVocabulary.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        if (!isLoadByNoted) {
            items = topic.getVocabularies() == null ? new ArrayList<>() : (ArrayList<Vocabulary>) topic.getVocabularies();
        }

        adapter = new VocabularyAdapter(items, appStatusService, topicService, requireContext(), isLoadByPublic, isLoadByNoted);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        registerForContextMenu(recyclerView);

        if (items.isEmpty()) {
            getData();
        } else {
            for (Vocabulary vocabulary : items) {
                vocabulary.setTopicId(topic.getId());
            }
        }
    }

    private void getData() {
        if (!isLoadByNoted) {
            getDataFromTopicId();
        } else {
            getDataFromUserId();
        }
    }

    private void getDataFromTopicId() {
        try {
            topicService.getTopicById(topic.getId()).addOnSuccessListener(queryDocumentSnapshots -> {
                topic = queryDocumentSnapshots.toObject(Topic.class);
                topic.setId(queryDocumentSnapshots.getId());
                items.clear();
                items.addAll(topic.getVocabularies());
                adapter.notifyDataSetChanged();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getDataFromUserId() {
        try {
            topicService.getTopicsByUserId(userService.getUserEmail()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null) {
                        topics.clear();
                        if (!querySnapshot.isEmpty()) {
                            for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                                Topic topic = documentSnapshot.toObject(Topic.class);
                                assert topic != null;
                                topic.setId(documentSnapshot.getId());
                                topics.add(topic);
                            }

                            items.clear();
                            for (Topic topic : topics) {
                                for (Vocabulary vocabulary : topic.getVocabularies()) {
                                    if (vocabulary.isNoted()) {
                                        vocabulary.setTopicId(topic.getId());
                                        items.add(vocabulary);
                                    }
                                }
                            }

                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setOnClickListener() {
        adapter.setOnVocabularyItemClickListener(new VocabularyAdapter.OnVocabularyItemClickListener() {
            @Override
            public void onItemLongClick(int position, Vocabulary vocabulary) {
                showContextMenu();
            }

            @Override
            public void onItemClick(int position, Vocabulary vocabulary) {

            }
        });

        binding.floatingActionButtonMainFragmentVocabulary.setOnClickListener(v -> {
            HomepageFragment addVocabularyFragment = new HomepageFragment(topic, null);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, addVocabularyFragment)
                    .addToBackStack(null)
                    .commit();
        });

        binding.buttonMainFragmentVocabularyQuizz.setOnClickListener(v -> {
            if (items == null ||items.isEmpty() || items.size() < 4) {
                Toast.makeText(getActivity(), "Chủ đề phải có ít nhất 4 từ vựng để thực hiện chức năng này!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(getActivity(), QuizzActivity.class);
                intent.putExtra("vocabularies", (Serializable) items);
                startActivity(intent);
            }
        });

        binding.buttonMainFragmentVocabularyWordFill.setOnClickListener(v -> {
            if (items == null ||items.isEmpty() || items.size() < 10) {
                Toast.makeText(getActivity(), "Chủ đề phải có ít nhất 10 từ vựng để thực hiện chức năng này!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(getActivity(), FillWordActivity.class);
                intent.putExtra("vocabularies", (Serializable) items);
                startActivity(intent);
            }
        });

        binding.buttonMainFragmentVocabularyFlashCard.setOnClickListener(v -> {
            if (items == null ||items.isEmpty()) {
                Toast.makeText(getActivity(), "Chủ đề rỗng!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(getActivity(), FlipCardActivity.class);
                intent.putExtra("vocabularies", (Serializable) items);
                startActivity(intent);
            }
        });
    }

    private void showContextMenu() {
        registerForContextMenu(recyclerView);
        getActivity().openContextMenu(recyclerView);
        unregisterForContextMenu(recyclerView);
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = requireActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_item_vocabulary, menu);

        if (isLoadByPublic || isLoadByNoted) {
            menu.findItem(R.id.menuItemVocabularyEdit).setVisible(false);
            menu.findItem(R.id.menuItemVocabularyDelete).setVisible(false);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menuItemVocabularyEdit) {
            int position = adapter.getLongClickedPosition();
            Vocabulary vocabulary = items.get(position);
            HomepageFragment editVocabularyFragment = new HomepageFragment(topic, vocabulary);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, editVocabularyFragment)
                    .addToBackStack(null)
                    .commit();

            return true;
        } else if (id == R.id.menuItemVocabularyDelete) {
            int position = adapter.getLongClickedPosition();
            Vocabulary vocabulary = items.get(position);
            showDeleteVocabularyDialog(vocabulary);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void showDeleteVocabularyDialog(Vocabulary vocabulary) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Xác nhận xoá từ vựng");
            builder.setMessage("Bạn có chắc chắn muốn xoá từ vựng này không?");

            builder.setPositiveButton("Xác nhận", (dialog, which) -> {
                items.remove(vocabulary);
                adapter.notifyDataSetChanged();
                topic.getVocabularies().remove(vocabulary);
                topicService.update(topic.getId(), topic);
            });

            builder.setNegativeButton("Huỷ", (dialog, which) -> dialog.cancel());

            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
