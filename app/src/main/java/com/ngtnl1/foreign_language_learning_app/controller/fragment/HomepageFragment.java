package com.ngtnl1.foreign_language_learning_app.controller.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;


import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.mlkit.nl.translate.Translator;
import com.ngtnl1.foreign_language_learning_app.common.OnFetchDataListener;
import com.ngtnl1.foreign_language_learning_app.controller.LoginActivity;
import com.ngtnl1.foreign_language_learning_app.controller.RegisterActivity;
import com.ngtnl1.foreign_language_learning_app.controller.adapter.MeaningsAdapter;
import com.ngtnl1.foreign_language_learning_app.controller.adapter.PhoneticsAdapter;
import com.ngtnl1.foreign_language_learning_app.databinding.FragmentMainHomepageBinding;
import com.ngtnl1.foreign_language_learning_app.model.APIResponse;
import com.ngtnl1.foreign_language_learning_app.model.Definition;
import com.ngtnl1.foreign_language_learning_app.model.Meaning;
import com.ngtnl1.foreign_language_learning_app.model.Phonetic;
import com.ngtnl1.foreign_language_learning_app.model.Topic;
import com.ngtnl1.foreign_language_learning_app.model.Vocabulary;
import com.ngtnl1.foreign_language_learning_app.service.AppStatusService;
import com.ngtnl1.foreign_language_learning_app.service.TopicService;
import com.ngtnl1.foreign_language_learning_app.service.VocabularyInfoRequestManager;
import com.ngtnl1.foreign_language_learning_app.service.UserService;


import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomepageFragment extends Fragment implements SearchView.OnQueryTextListener {
    @Inject
    VocabularyInfoRequestManager manager;
    @Inject
    AppStatusService appStatusService;
    @Inject
    UserService userService;
    @Inject
    Translator translator;
    @Inject
    TopicService topicService;
    private FragmentMainHomepageBinding binding;
    ProgressDialog progressDialog;
    private RecyclerView recyclerViewHomepagePhonetics;
    private RecyclerView recyclerViewHomepageMeanings;
    private PhoneticsAdapter phoneticsAdapter;
    private MeaningsAdapter meaningsAdapter;
    private Topic topic;
    private Vocabulary vocabulary;
    String word;
    List<Phonetic> phonetics;
    List<Meaning> meanings;

    public HomepageFragment(Topic topic, Vocabulary vocabulary) {
        this.topic = topic;
        this.vocabulary = vocabulary;

    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMainHomepageBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();
        setOnClickListener();
        translator.downloadModelIfNeeded();
    }

    private void initViews() {
        progressDialog = new ProgressDialog(requireContext());
        recyclerViewHomepagePhonetics = binding.recyclerViewHomepagePhonetics;
        recyclerViewHomepageMeanings = binding.recyclerViewHomepageMeanings;

        if (userService.isUserSignedIn()) {
            binding.linearLayoutMainHomepageAuthenticationSection.setVisibility(View.GONE);
        } else {
            binding.linearLayoutMainHomepageAuthenticationSection.setVisibility(View.VISIBLE);
        }

        if (topic != null) {
            binding.linearLayoutMainHomepageVocabularySection.setVisibility(View.VISIBLE);

            // set item for type word type spinner
            String[] wordTypes = {"Danh từ", "Động từ", "Tính từ", "Trạng từ", "Giới từ", "Liên từ", "Giới từ", "Thán từ", "Đại từ", "Số từ", "Từ nối", "Từ hỏi", "Từ chỉ", "Từ đặc biệt"};
            ArrayAdapter<String> wordTypeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, wordTypes);
            wordTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerVocabularyType.setAdapter(wordTypeAdapter);
        }

        if (vocabulary != null) {
            binding.linearLayoutMainHomepageVocabularySection.setVisibility(View.VISIBLE);
            binding.editTextVocabularyWord.setText(vocabulary.getWord());
            binding.editTextVocabularyMeaning.setText(vocabulary.getMeaning());
            binding.editTextVocabularyPhonetic.setText(vocabulary.getPhonetic());
            binding.editTextVocabularyDescription.setText(vocabulary.getDescription());
            binding.editTextVocabularyExample.setText(vocabulary.getExample());
            for (int i = 0; i < binding.spinnerVocabularyType.getCount(); i++) {
                if (binding.spinnerVocabularyType.getItemAtPosition(i).toString().equalsIgnoreCase(vocabulary.getType())) {
                    binding.spinnerVocabularyType.setSelection(i);
                    break;
                }
            }

            // search for word
            binding.searchViewHomepage.setQuery(vocabulary.getWord(), true);
        }
    }

    private void setOnClickListener() {
        // vocabulary section
        if (vocabulary == null) {
            binding.buttonVocabularySave.setOnClickListener(v -> addVocabulary());
        } else {
            binding.buttonVocabularySave.setText("Cập nhật");
            binding.buttonVocabularySave.setOnClickListener(v -> editVocabulary(vocabulary));
        }

        // vocabulary info suggestion section
        binding.imageButtonGetWord.setOnClickListener(v -> showWordSuggestionDialog());
        binding.imageButtonGetMeaning.setOnClickListener(v -> showVietNamMeaningSuggestionDialog());
        binding.imageButtonGetPhonetic.setOnClickListener(v -> showPhoneticSuggestionDialog());
        binding.imageButtonGetDescription.setOnClickListener(v -> showDescriptionSuggestionDialog());

        // authentication section
        binding.buttonHomepageLogin.setOnClickListener(v -> changeToLogin());
        binding.buttonHomepageRegister.setOnClickListener(v -> changeToRegister());

        // search vocabulary section
        binding.searchViewHomepage.setOnQueryTextListener(this);

    }

    private void addVocabulary() {
        if (isValidInput()) {
            // Create a new Vocabulary object
            Vocabulary newVocabulary = new Vocabulary();
            newVocabulary.setWord(binding.editTextVocabularyWord.getText().toString());
            newVocabulary.setMeaning(binding.editTextVocabularyMeaning.getText().toString());
            newVocabulary.setPhonetic(binding.editTextVocabularyPhonetic.getText().toString());
            newVocabulary.setType(binding.spinnerVocabularyType.getSelectedItem().toString());
            newVocabulary.setDescription(binding.editTextVocabularyDescription.getText().toString());
            newVocabulary.setExample(binding.editTextVocabularyExample.getText().toString());

            for (Vocabulary vocabulary : topic.getVocabularies()) {
                if (vocabulary.getWord().equalsIgnoreCase(binding.editTextVocabularyWord.getText().toString())) {
                    Toast.makeText(getContext(), "Từ vựng đã tồn tại!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            topic.getVocabularies().add(newVocabulary);
            topicService.update(topic.getId(), topic);

            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private void editVocabulary(Vocabulary vocabulary) {
        if (isValidInput()) {
            vocabulary.setWord(binding.editTextVocabularyWord.getText().toString());
            vocabulary.setMeaning(binding.editTextVocabularyMeaning.getText().toString());
            vocabulary.setPhonetic(binding.editTextVocabularyPhonetic.getText().toString());
            vocabulary.setType(binding.spinnerVocabularyType.getSelectedItem().toString());
            vocabulary.setDescription(binding.editTextVocabularyDescription.getText().toString());
            vocabulary.setExample(binding.editTextVocabularyExample.getText().toString());

            int index = 0;

            for (Vocabulary v : topic.getVocabularies()) {
                if (v.getWord().equalsIgnoreCase(binding.editTextVocabularyWord.getText().toString())) {
                    index++;
                }
            }

            if (index > 1) {
                Toast.makeText(getContext(), "Từ vựng đã tồn tại!", Toast.LENGTH_SHORT).show();
                return;
            }

            topicService.update(topic.getId(), topic);

            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private boolean isValidInput() {
        if (binding.editTextVocabularyWord.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Từ vựng không được để trống!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (binding.editTextVocabularyMeaning.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Nghĩa của từ vựng không được để trống!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void showWordSuggestionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Từ vựng");

        if (word == null || word.isEmpty()) {
            builder.setMessage("Không có từ vựng được tìm thấy");
        } else {
            builder.setMessage("Bạn có muốn sử dụng " + word + " làm từ vựng không?")
                    .setPositiveButton("Có", (dialog, id) -> {
                        binding.editTextVocabularyWord.setText(word);
                    })
                    .setNegativeButton("Không", (dialog, id) -> {
                        dialog.dismiss();
                    });
        }

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showVietNamMeaningSuggestionDialog() {
        try {
            String vietnameseMeaning = binding.textViewHomepageVietNamMeaning.getText().toString();

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Nghĩa tiếng Việt");

            if (vietnameseMeaning == null || vietnameseMeaning.isEmpty()) {
                builder.setMessage("Không có nghĩa tiếng Việt được tìm thấy");
            } else {
                builder.setMessage("Bạn có muốn sử dụng " + vietnameseMeaning + " làm nghĩa tiếng Việt không?")
                        .setPositiveButton("Có", (dialog, id) -> {
                            binding.editTextVocabularyMeaning.setText(vietnameseMeaning);
                        })
                        .setNegativeButton("Không", (dialog, id) -> {
                            dialog.dismiss();
                        });
            }

            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPhoneticSuggestionDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Phiên âm");

            if (phonetics == null || phonetics.isEmpty()) {
                builder.setMessage("Không có phiên âm nào được tìm thấy");
            } else {
                String[] phoneticTexts = new String[phonetics.size()];

                for (int i = 0; i < phonetics.size(); i++) {
                    phoneticTexts[i] = phonetics.get(i).getText();
                }

                builder.setItems(phoneticTexts, (dialog, which) -> {
                    binding.editTextVocabularyPhonetic.setText(phoneticTexts[which]);
                });
            }

            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDescriptionSuggestionDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Định nghĩa");

            if (meanings == null || meanings.isEmpty()) {
                builder.setMessage("Không có định nghĩa nào được tìm thấy");
            } else {
                List<Definition> definitions = new ArrayList<>();

                for (Meaning meaning : meanings) {
                    definitions.addAll(meaning.getDefinitions());
                }

                String[] meaningTexts = new String[definitions.size()];

                for (int i = 0; i < definitions.size(); i++) {
                    meaningTexts[i] = definitions.get(i).getDefinition();
                }

                builder.setItems(meaningTexts, (dialog, which) -> {
                    binding.editTextVocabularyDescription.setText(meaningTexts[which]);

                    if (definitions.get(which).getExample() != null && !definitions.get(which).getExample().isEmpty()) {
                        binding.editTextVocabularyExample.setText(definitions.get(which).getExample());
                    }
                });
            }

            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeToRegister() {
        Intent intent = new Intent(getActivity(), RegisterActivity.class);
        startActivity(intent);
    }

    private void changeToLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }

    private final OnFetchDataListener listener = new OnFetchDataListener() {
        @Override
        public void OnFetchData(APIResponse apiResponse, String message) {
            progressDialog.dismiss();

            if(apiResponse == null){
                Toast.makeText(getActivity().getApplicationContext(), "Không tìm thấy dữ liệu của từ vựng!", Toast.LENGTH_SHORT).show();
                return;
            }

            showSearchResult(apiResponse);
        }

        @Override
        public void OnError(String message) {
            progressDialog.dismiss();
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    };

    private void showSearchResult(APIResponse apiResponse) {
        binding.textViewHomepageWord.setText("Kết quả cho '" + apiResponse.getWord() + "':");
        word = apiResponse.getWord();
        // recycler view for phonetics
        recyclerViewHomepagePhonetics.setHasFixedSize(true);
        recyclerViewHomepagePhonetics.setLayoutManager(new GridLayoutManager(getContext(), 1));
        phoneticsAdapter = new PhoneticsAdapter(getContext(), apiResponse.getPhonetics(), appStatusService);
        recyclerViewHomepagePhonetics.setAdapter(phoneticsAdapter);
        phonetics = apiResponse.getPhonetics();
        // recycler view for meanings
        recyclerViewHomepageMeanings.setHasFixedSize(true);
        recyclerViewHomepageMeanings.setLayoutManager(new GridLayoutManager(getContext(), 1));
        meaningsAdapter = new MeaningsAdapter(getContext(), apiResponse.getMeanings());
        recyclerViewHomepageMeanings.setAdapter(meaningsAdapter);
        meanings = apiResponse.getMeanings();
        // translate to vietnamese
        translator.downloadModelIfNeeded()
                .addOnSuccessListener(v -> {
                    binding.textViewHomepageVietNamMeaning.setText("Đang dịch ....");
                    translator.translate(apiResponse.getWord())
                            .addOnSuccessListener(s -> {
                                binding.textViewHomepageVietNamMeaning.setText(s);
                            })
                            .addOnFailureListener(e -> {
                                binding.textViewHomepageVietNamMeaning.setText("");
                                Toast.makeText(getContext(), "Không thể dịch từ vựng!", Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Không thể tải dữ liệu từ vựng!", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (!appStatusService.isOnline()) {
            Toast.makeText(getContext(), "Không có kết nối Internet!", Toast.LENGTH_SHORT).show();
            return false;
        }

        progressDialog.setTitle("Đang tải kết quả cho từ vựng: " + query);
        progressDialog.show();
        manager.getWordMeaning(listener, query);

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
