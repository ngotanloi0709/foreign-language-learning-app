package com.ngtnl1.foreign_language_learning_app.controller.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.ngtnl1.foreign_language_learning_app.controller.adapter.TopicAdapter;
import com.ngtnl1.foreign_language_learning_app.databinding.FragmentMainTopicBinding;
import com.ngtnl1.foreign_language_learning_app.model.Folder;
import com.ngtnl1.foreign_language_learning_app.model.Topic;
import com.ngtnl1.foreign_language_learning_app.model.User;
import com.ngtnl1.foreign_language_learning_app.service.AppStatusService;
import com.ngtnl1.foreign_language_learning_app.service.FolderService;
import com.ngtnl1.foreign_language_learning_app.service.TopicService;
import com.ngtnl1.foreign_language_learning_app.service.UserService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TopicFragment extends Fragment {
    @Inject
    TopicService topicService;
    @Inject
    FolderService folderService;
    @Inject
    UserService userService;
    @Inject
    AppStatusService appStatusService;
    private final Folder folder;
    private final User user;
    private final boolean isLoadByFolder;
    private final boolean isLoadByPublic;
    private FragmentMainTopicBinding binding;
    private List<Topic> items = new ArrayList<>();

    private TopicAdapter adapter;
    private RecyclerView recyclerView;

    public TopicFragment(User user, Folder folder, boolean isLoadByFolder, boolean isLoadByPublic, List<Topic> topics) {
        this.user = user;
        this.folder = folder;
        this.isLoadByFolder = isLoadByFolder;
        this.isLoadByPublic = isLoadByPublic;

        if (topics != null && !isLoadByFolder) {
            this.items = topics;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMainTopicBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();
        setupRecyclerView();
        setOnClickListener();
        sortTopic();

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setEnabled(false);
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void initViews() {
        if (isLoadByFolder) {
            binding.textViewMainFragmentTopicName.setText("Danh sách chủ đề của thư mục " + folder.getName());
        } else if (isLoadByPublic) {
            binding.textViewMainFragmentTopicName.setText("Danh sách các chủ đề công khai");
            binding.floatingActionButtonMainFragmentTopic.setVisibility(View.GONE);
        } else {
            binding.textViewMainFragmentTopicName.setText("Danh sách chủ đề của " + user.getName());
        }

        recyclerView = binding.recyclerViewMainFragmentTopic;
    }
    private void sortTopic() {
        Spinner spinnerSortTopic = requireView().findViewById(R.id.spinnerSortTopic);

        List<String> sortType = new ArrayList<>();
        sortType.add("Xếp hạng theo lượt truy cập");
        sortType.add("Xếp hạng theo tên: A -> Z");
        sortType.add("Xếp hạng theo tên: Z -> A");

        ArrayAdapter<String> spinnerAdapterSort = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, sortType);
        spinnerAdapterSort.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortTopic.setAdapter(spinnerAdapterSort);

        spinnerSortTopic.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedSortOption = sortType.get(position);
                performSort(selectedSortOption);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });
    }

    private void performSort(String selectedSortOption) {
        if (items != null && adapter != null) {
            switch (selectedSortOption) {
                case "Xếp hạng theo lượt truy cập":
                    Collections.sort(items, (topic1, topic2) -> Integer.compare(topic2.getViews(), topic1.getViews()));
                    break;
                case "Xếp hạng theo tên: A -> Z":
                    Collections.sort(items, (s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName()));
                    break;
                case "Xếp hạng theo tên: Z -> A":
                    Collections.sort(items, (s1, s2) -> s2.getName().compareToIgnoreCase(s1.getName()));
                    break;
            }
            adapter.notifyDataSetChanged();
        }
    }


    private void setupRecyclerView() {
        adapter = new TopicAdapter(items, isLoadByPublic);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        registerForContextMenu(recyclerView);

        if (items.isEmpty()) {
            getData();
        } else {
            performSort("Xếp hạng theo lượt truy cập");
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getData() {
        if (isLoadByFolder) {
            getDataFromFolder();
        } else if (isLoadByPublic) {
            getDataFromPublicTopic();
        } else {
            getDataFromUser();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getDataFromFolder() {
        try {
            topicService.findTopicsById(folder.getTopics()).addOnSuccessListener(topics -> {
                if (topics != null) {
                    items.clear();
                    if (!topics.isEmpty()) {
                        items.addAll(topics);
                        performSort("Xếp hạng theo lượt truy cập");
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getDataFromPublicTopic() {
        topicService.findTopicsByPublic().addOnSuccessListener(topics -> {
            if (topics != null) {
                items.clear();
                if (!topics.isEmpty()) {
                    items.addAll(topics);
                    performSort("Xếp hạng theo lượt truy cập");
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getDataFromUser() {
        try {
            topicService.getTopicsByUserId(user.getEmail()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null) {
                        items.clear();
                        if (!querySnapshot.isEmpty()) {
                            for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                                Topic topic = documentSnapshot.toObject(Topic.class);
                                assert topic != null;
                                topic.setId(documentSnapshot.getId());
                                items.add(topic);
                            }
                            performSort("Xếp hạng theo lượt truy cập");
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
        adapter.setOnTopicItemClickListener(new TopicAdapter.OnTopicItemClickListener() {
            @Override
            public void onItemLongClick(int position, Topic topic) {
                showContextMenu();
            }

            @Override
            public void onItemClick(int position, Topic topic) {
                // Update view count
                topic.incrementViews();
                // Update the topic in the database
                topicService.update(topic.getId(), topic);
                // Update topic
                topicService.update(topic.getId(), topic);
                // Navigate to VocabularyFragment
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView, new VocabularyFragment(topic, isLoadByPublic, false))
                        .addToBackStack(null)
                        .commit();
            }
        });

        if (!isLoadByFolder) {
            binding.floatingActionButtonMainFragmentTopic.setOnClickListener(v -> showAddTopicDialog());
        } else {
            binding.floatingActionButtonMainFragmentTopic.setOnClickListener(v -> showOptionAddTopicDialog());
        }
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
        inflater.inflate(R.menu.menu_item_topic, menu);

        if (!isLoadByFolder) {
            MenuItem item = menu.findItem(R.id.menuItemTopicDeleteFromFolder);
            item.setVisible(false);
        }

        if (isLoadByPublic) {
            MenuItem item = menu.findItem(R.id.menuItemTopicEdit);
            item.setVisible(false);
            item = menu.findItem(R.id.menuItemTopicDelete);
            item.setVisible(false);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        int position = adapter.getLongClickedPosition();
        Topic topic = items.get(position);

        if (id == R.id.menuItemTopicEdit) {
            showEditTopicDialog(topic);
            return true;
        } else if (id == R.id.menuItemTopicDelete) {
            showDeleteTopicDialog(topic);
            return true;
        } else if (id == R.id.menuItemTopicDeleteFromFolder) {
            showDeleteFromFolderTopicDialog(topic);
            return true;
        }

        return super.onContextItemSelected(item);
    }

    private void showOptionAddTopicDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Thêm chủ đề");

            String[] options = {"Thêm topic mới", "Thêm topic có sẵn"};
            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        showAddTopicDialog();
                        break;
                    case 1:
                        showAddExistingTopicDialog();
                        break;
                }
            });

            builder.create().show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAddExistingTopicDialog() {
        topicService.getTopicsByUserId(user.getEmail()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    List<Topic> topics = new ArrayList<>();

                    for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                        Topic topic = documentSnapshot.toObject(Topic.class);
                        assert topic != null;
                        topic.setId(documentSnapshot.getId());
                        topics.add(topic);
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle("Thêm chủ đề có sẵn");

                    String[] options = new String[topics.size()];
                    for (int i = 0; i < topics.size(); i++) {
                        options[i] = topics.get(i).getName();
                    }

                    builder.setItems(options, (dialog, which) -> {
                        Topic topic = topics.get(which);

                        if (folder.getTopics() == null) {
                            folder.setTopics(new ArrayList<>());
                        }

                        if (folder.getTopics().contains(topic.getId())) {
                            Toast.makeText(requireContext(), "Chủ đề này đã có trong thư mục", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        items.add(topic);
                        performSort("Xếp hạng theo lượt truy cập");
                        adapter.notifyDataSetChanged();
                        folder.getTopics().add(topic.getId());
                        folderService.update(folder.getId(), folder);
                        Toast.makeText(requireContext(), "Thêm chủ đề thành công", Toast.LENGTH_SHORT).show();
                    });

                    builder.create().show();
                }
            }
        });
    }

    private void showAddTopicDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_add_topic, null);

            EditText editTextName = view.findViewById(R.id.editTextDialogAddTopicName);
            EditText editTextDescription = view.findViewById(R.id.editTextDialogAddTopicDescription);
            CheckBox checkBoxPublic = view.findViewById(R.id.checkBoxDialogAddTopicPublic);

            builder.setView(view)
                    .setTitle("Thêm chủ đề mới")
                    .setPositiveButton("Thêm", (dialog, id) -> {
                        String name = editTextName.getText().toString();
                        String description = editTextDescription.getText().toString();
                        boolean isPublic = checkBoxPublic.isChecked();

                        addNewTopic(new Topic(user.getEmail(), name, description, isPublic, new ArrayList<>()));
                    });
            builder.setNegativeButton("Huỷ", (dialog, which) -> dialog.cancel());

            builder.create().show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addNewTopic(Topic topic) {
        items.add(topic);
        performSort("Xếp hạng theo lượt truy cập");
        adapter.notifyDataSetChanged();

        topicService.create(topic).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (isLoadByFolder) {
                    String newTopicId = task.getResult().getId();

                    if (folder.getTopics() == null) {
                        folder.setTopics(new ArrayList<>());
                    }

                    folder.getTopics().add(newTopicId);
                    folderService.update(folder.getId(), folder);
                }

                getData();
            }
        });

        if (!appStatusService.isOnline()) {
            getData();
        }

        Toast.makeText(requireContext(), "Thêm chủ đề thành công", Toast.LENGTH_SHORT).show();
    }

    private void showEditTopicDialog(Topic topic) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_add_topic, null);

            EditText editTextName = view.findViewById(R.id.editTextDialogAddTopicName);
            EditText editTextDescription = view.findViewById(R.id.editTextDialogAddTopicDescription);
            CheckBox checkBoxPublic = view.findViewById(R.id.checkBoxDialogAddTopicPublic);
            editTextName.setText(topic.getName());
            editTextDescription.setText(topic.getDescription());
            checkBoxPublic.setChecked(topic.isPublic());

            builder.setView(view)
                    .setTitle("Chỉnh sửa chủ đề")
                    .setPositiveButton("Xác nhận", (dialog, id) -> {
                        String name = editTextName.getText().toString();
                        boolean isPublic = checkBoxPublic.isChecked();

                        topic.setName(name);
                        topic.setPublic(isPublic);
                        topic.setDescription(editTextDescription.getText().toString());

                        adapter.notifyDataSetChanged();
                        topicService.update(topic.getId(), topic);
                        Toast.makeText(requireContext(), "Cập nhật chủ đề thành công", Toast.LENGTH_SHORT).show();
                    });
            builder.setNegativeButton("Huỷ", (dialog, which) -> dialog.cancel());

            builder.create().show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDeleteTopicDialog(Topic topic) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Xác nhận xoá chủ đề");
            builder.setMessage("Bạn có chắc chắn muốn xoá chủ đề này không?");

            builder.setPositiveButton("Xác nhận", (dialog, which) -> deleteTopic(topic));
            builder.setNegativeButton("Huỷ", (dialog, which) -> dialog.cancel());

            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void deleteTopic(Topic deleteTopic) {
        if (deleteTopic.getId() == null) {
            Toast.makeText(requireContext(), "Chủ đề chưa khởi tạo xong", Toast.LENGTH_SHORT).show();
            return;
        }

        items.remove(deleteTopic);
        adapter.notifyDataSetChanged();
        topicService.remove(deleteTopic.getId());
        Toast.makeText(requireContext(), "Xoá chủ đề thành công", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteFromFolderTopicDialog(Topic topic) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Xác nhận xoá chủ đề");
            builder.setMessage("Bạn có chắc chắn muốn xoá chủ đề này không?");

            builder.setPositiveButton("Xác nhận", (dialog, which) -> deleteTopicFromFolder(topic));
            builder.setNegativeButton("Huỷ", (dialog, which) -> dialog.cancel());

            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void deleteTopicFromFolder(Topic topic) {
        if (folder.getTopics() != null) {
            items.remove(topic);
            adapter.notifyDataSetChanged();
            folder.getTopics().remove(topic.getId());
            folderService.update(folder.getId(), folder);
            Toast.makeText(requireContext(), "Xoá chủ đề khỏi thư mục thành công", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!appStatusService.isOnline()) {
            adapter.notifyDataSetChanged();
        }

        getData();
    }
}
