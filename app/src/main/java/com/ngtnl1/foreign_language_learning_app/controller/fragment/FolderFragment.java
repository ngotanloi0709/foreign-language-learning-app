package com.ngtnl1.foreign_language_learning_app.controller.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ngtnl1.foreign_language_learning_app.R;
import com.ngtnl1.foreign_language_learning_app.controller.adapter.FolderAdapter;
import com.ngtnl1.foreign_language_learning_app.databinding.FragmentMainFolderBinding;
import com.ngtnl1.foreign_language_learning_app.model.Folder;
import com.ngtnl1.foreign_language_learning_app.model.User;
import com.ngtnl1.foreign_language_learning_app.service.AppStatusService;
import com.ngtnl1.foreign_language_learning_app.service.FolderService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FolderFragment extends Fragment {
    @Inject
    FolderService folderService;
    @Inject
    AppStatusService appStatusService;
    private final User user;
    private List<Folder> items = new ArrayList<>();
    private FolderAdapter adapter;
    private RecyclerView recyclerView;
    private FragmentMainFolderBinding binding;

    public FolderFragment(User user, List<Folder> folders) {
        this.user = user;

        if (folders != null) {
            this.items = folders;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMainFolderBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();
        setupRecyclerView();
        setOnClickListener();
        sortFolder();

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setEnabled(false);
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void initViews() {
        recyclerView = binding.recyclerViewMainFragmentFolder;
        binding.textViewMainFragmentFolderName.setText("Danh sách thư mục của " + user.getName());
    }

    private void setupRecyclerView() {
        adapter = new FolderAdapter(items);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        registerForContextMenu(recyclerView);

        if (items.isEmpty()) {
            getData();
        } else {
            performSort("Xếp hạng theo tên: A -> Z");
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getData() {
        try {
            folderService.getFoldersByUserId(user.getEmail()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null) {
                        items.clear();
                        if (!querySnapshot.isEmpty()) {
                            for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                                Folder folder = documentSnapshot.toObject(Folder.class);
                                assert folder != null;
                                folder.setId(documentSnapshot.getId());
                                items.add(folder);
                            }
                            performSort("Xếp hạng theo tên: A -> Z");
                            adapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Lỗi khi truy cập thư mục: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setOnClickListener() {
        adapter.setOnFolderItemClickListener(new FolderAdapter.OnFolderItemClickListener() {
            @Override
            public void onItemLongClick(int position, Folder folder) {
                showContextMenu();
            }

            @Override
            public void onItemClick(int position, Folder folder) {
                showTopicFragment(folder);
            }
        });

        binding.floatingActionButtonMainFragmentFolder.setOnClickListener(v -> showAddFolderDialog());
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
        inflater.inflate(R.menu.menu_item_folder, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menuItemFolderEdit) {
            int position = adapter.getLongClickedPosition();
            Folder folder = items.get(position);
            showEditFolderDialog(folder);
            return true;
        } else if (id == R.id.menuItemFolderDelete) {
            int position = adapter.getLongClickedPosition();
            Folder folder = items.get(position);
            showDeleteFolderDialog(folder);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void showAddFolderDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Thêm thư mục mới");

            final EditText input = new EditText(requireContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("Thêm", (dialog, which) -> {
                String folderName = input.getText().toString();
                if (!folderName.isEmpty()) {
                    addNewFolder(folderName);
                } else {
                    Toast.makeText(requireContext(), "Tên thư mục không được để trống", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Huỷ", (dialog, which) -> dialog.cancel());

            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addNewFolder(String folderName) {
        Folder newFolder = new Folder();
        newFolder.setName(folderName);
        newFolder.setUserId(user.getEmail());

        items.add(newFolder);
        performSort("Xếp hạng theo tên: A -> Z");
        adapter.notifyDataSetChanged();

        folderService.create(newFolder).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                getData();
            }
        });

        if (!appStatusService.isOnline()) {
            getData();
        }

        Toast.makeText(requireContext(), "Thư mục mới đã được tạo", Toast.LENGTH_SHORT).show();
    }

    private void showEditFolderDialog(Folder folder) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Chỉnh sửa tên thư mục");

            final EditText input = new EditText(requireContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(folder.getName());
            builder.setView(input);

            builder.setPositiveButton("Xác nhận", (dialog, which) -> {
                String newName = input.getText().toString();
                if (!newName.isEmpty()) {
                    updateFolderName(folder, newName);
                } else {
                    Toast.makeText(requireContext(), "Tên thư mục không được để trống", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Huỷ", (dialog, which) -> dialog.cancel());

            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateFolderName(Folder folder, String newName) {
        folder.setName(newName);
        adapter.notifyDataSetChanged();
        folderService.update(folder.getId(), folder);
        Toast.makeText(requireContext(), "Tên thư mục đã được cập nhật", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteFolderDialog(Folder folder) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Xác nhận xoá thư mục");
            builder.setMessage("Bạn có chắc chắn muốn xoá thư mục này không?");

            builder.setPositiveButton("Xác nhận", (dialog, which) -> deleteFolder(folder));
            builder.setNegativeButton("Huỷ", (dialog, which) -> dialog.cancel());

            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void deleteFolder(Folder folder) {
        if (folder.getId() == null) {
            Toast.makeText(requireContext(), "Thư mục chưa được khởi tạo xong", Toast.LENGTH_SHORT).show();
            return;
        }
        items.remove(folder);
        adapter.notifyDataSetChanged();
        folderService.remove(folder.getId());
        Toast.makeText(requireContext(), "Thư mục đã được xoá", Toast.LENGTH_SHORT).show();
    }

    private void showTopicFragment(Folder folder) {
        try {
            Fragment selectedFragment = new TopicFragment(user, folder, true, false, null);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, selectedFragment)
                    .addToBackStack(null)
                    .commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
    }

    private void sortFolder() {
        Spinner spinnerSortFolder = requireView().findViewById(R.id.spinnerSortFolder);

        List<String> sortType = new ArrayList<>();
        sortType.add("Xếp hạng theo tên: A -> Z");
        sortType.add("Xếp hạng theo tên: Z -> A");

        ArrayAdapter<String> spinnerAdapterSort = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, sortType);
        spinnerAdapterSort.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortFolder.setAdapter(spinnerAdapterSort);

        spinnerSortFolder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
}
