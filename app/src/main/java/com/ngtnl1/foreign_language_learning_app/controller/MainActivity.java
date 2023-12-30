package com.ngtnl1.foreign_language_learning_app.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.ngtnl1.foreign_language_learning_app.R;
import com.ngtnl1.foreign_language_learning_app.controller.fragment.FolderFragment;
import com.ngtnl1.foreign_language_learning_app.controller.fragment.HomepageFragment;
import com.ngtnl1.foreign_language_learning_app.controller.fragment.ProfileManagementFragment;
import com.ngtnl1.foreign_language_learning_app.controller.fragment.TopicFragment;
import com.ngtnl1.foreign_language_learning_app.controller.fragment.VocabularyFragment;
import com.ngtnl1.foreign_language_learning_app.databinding.ActivityMainBinding;
import com.ngtnl1.foreign_language_learning_app.model.Folder;
import com.ngtnl1.foreign_language_learning_app.model.Topic;
import com.ngtnl1.foreign_language_learning_app.model.User;
import com.ngtnl1.foreign_language_learning_app.service.AppStatusService;
import com.ngtnl1.foreign_language_learning_app.service.FolderService;
import com.ngtnl1.foreign_language_learning_app.service.TopicService;
import com.ngtnl1.foreign_language_learning_app.service.UserService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    @Inject
    UserService userService;
    @Inject
    FolderService folderService;
    @Inject
    TopicService topicService;
    @Inject
    StorageReference storageReference;
    @Inject
    AppStatusService appStatusService;
    private User user;
    private List<Folder> folders = new ArrayList<>();
    private List<Topic> topics = new ArrayList<>();
    private List<Topic> publicTopics = new ArrayList<>();
    private ActivityMainBinding binding;
    private ShapeableImageView imageNavigationAvatar;
    private TextView textViewNavigationName;
    private TextView textViewNavigationEmail;
    private MenuItem menuItemNavigationProfileManagement;
    private MenuItem menuItemNavigationLogin;
    private MenuItem menuItemNavigationLogout;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initViews();
        configureDrawer();
        setDefaultFragment();
        getData();
    }

    private void initViews() {
        // setup view binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // setup layout
        setSupportActionBar(binding.toolbar);
        binding.navigationView.setNavigationItemSelectedListener(this);

        // setup header navigation views
        imageNavigationAvatar = binding.navigationView.getHeaderView(0).findViewById(R.id.imageNavigationAvatar);
        textViewNavigationName = binding.navigationView.getHeaderView(0).findViewById(R.id.textNavigationViewName);
        textViewNavigationEmail = binding.navigationView.getHeaderView(0).findViewById(R.id.textViewNavigationEmail);

        // setup menu items
        menuItemNavigationProfileManagement = binding.navigationView.getMenu().findItem(R.id.menuItemNavigationProfileManagement);
        menuItemNavigationLogin = binding.navigationView.getMenu().findItem(R.id.menuItemNavigationLogin);
        menuItemNavigationLogout = binding.navigationView.getMenu().findItem(R.id.menuItemNavigationLogout);

        // setup bottom navigation view
        bottomNavigationView = findViewById(R.id.bottomNavigationView); // replace with your BottomNavigationView's ID
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onBottomNavigationItemSelected);
    }

    private void configureDrawer() {
        // add toggle button
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setDefaultFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainerView, new HomepageFragment(null, null))
                .addToBackStack(null)
                .commit();
        binding.navigationView.setCheckedItem(R.id.menuItemNavigationHomepage);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.menuItemNavigationHomepage) {
            selectedFragment = new HomepageFragment(null, null);
        } else if (itemId == R.id.menuItemNavigationProfileManagement) {
            if (userService.isUserSignedIn()) {
                if (user == null) {
                    getData();
                    return false;
                }

                selectedFragment = new ProfileManagementFragment(user, ((BitmapDrawable) imageNavigationAvatar.getDrawable()).getBitmap());
            } else {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                Toast.makeText(this, "Bạn cần đăng nhập để sử dụng tính năng này!", Toast.LENGTH_SHORT).show();
            }
        } else if (itemId == R.id.menuItemNavigationNotedWords) {
            if (userService.isUserSignedIn()) {
                if (user == null) {
                    getData();
                    return false;
                }

                selectedFragment = new VocabularyFragment(null, false, true);
            } else {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                Toast.makeText(this, "Bạn cần đăng nhập để sử dụng tính năng này!", Toast.LENGTH_SHORT).show();
            }
        } else if (itemId == R.id.menuItemNavigationLogin) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        } else if (itemId == R.id.menuItemNavigationLogout) {
            FirebaseAuth.getInstance().signOut();
            getData();
            startActivity(new Intent(MainActivity.this, MainActivity.class));
            finish();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, selectedFragment)
                    .addToBackStack(null)
                    .commit();
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @SuppressLint("SetTextI18n")
    public void getData() {
        try {
            boolean isLogged = userService.isUserSignedIn();

            menuItemNavigationLogin.setVisible(!isLogged);
            menuItemNavigationProfileManagement.setVisible(isLogged);
            menuItemNavigationLogout.setVisible(isLogged);

            if (isLogged) {
                userService.getUserDataRaw().addOnSuccessListener(documentSnapshot -> {
                    user = documentSnapshot.toObject(User.class);

                    if (user != null) {
                        textViewNavigationEmail.setText(user.getEmail());
                        textViewNavigationName.setText(user.getName());
                    }

                    folderService.getFoldersByUserId(user.getEmail()).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null) {
                                folders.clear();
                                if (!querySnapshot.isEmpty()) {
                                    for (DocumentSnapshot d : querySnapshot.getDocuments()) {
                                        Folder folder = d.toObject(Folder.class);
                                        assert folder != null;
                                        folder.setId(d.getId());
                                        folders.add(folder);
                                    }
                                }
                            }
                        }
                    });

                    topicService.findTopicsByPublic().addOnSuccessListener(topics_ref -> {
                        if (topics_ref != null) {
                            publicTopics.clear();

                            if (!topics_ref.isEmpty()) {
                                publicTopics.addAll(topics_ref);
                            }
                        }
                    });

                    topicService.getTopicsByUserId(user.getEmail()).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null) {
                                topics.clear();
                                if (!querySnapshot.isEmpty()) {
                                    for (DocumentSnapshot d : querySnapshot.getDocuments()) {
                                        Topic topic = d.toObject(Topic.class);
                                        assert topic != null;
                                        topic.setId(d.getId());
                                        topics.add(topic);
                                    }
                                }
                            }
                        }
                    });
            });

            if (appStatusService.isOnline()) {
                    storageReference.child("images/" + userService.getUserEmail() + ".jpg").getDownloadUrl().addOnSuccessListener(uri -> Glide.with(this).load(uri).into(imageNavigationAvatar)).addOnFailureListener(exception -> Glide.with(this).load(R.drawable.img_sample_avatar).into(imageNavigationAvatar));
                }
            } else {
                textViewNavigationName.setText("Chưa đăng nhập");
                textViewNavigationEmail.setText("chúng tôi không biết bạn là ai!");
                Glide.with(this).load(R.drawable.img_sample_avatar).into(imageNavigationAvatar);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean onBottomNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        if (item.getItemId() == R.id.menuItemBottomFolder) {
            if (!userService.isUserSignedIn()) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                Toast.makeText(this, "Bạn cần đăng nhập để sử dụng tính năng này!", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (user == null) {
                getData();
                return false;
            }

            selectedFragment = new FolderFragment(user, folders);
        } else if (item.getItemId() == R.id.menuItemBottomTopic) {
            if (!userService.isUserSignedIn()) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                Toast.makeText(this, "Bạn cần đăng nhập để sử dụng tính năng này!", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (user == null) {
                getData();
                return false;
            }

            selectedFragment = new TopicFragment(user, null, false, false, topics);
        } else if (item.getItemId() == R.id.menuItemBottomPublicTopic) {
            if (!userService.isUserSignedIn()) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                Toast.makeText(this, "Bạn cần đăng nhập để sử dụng tính năng này!", Toast.LENGTH_SHORT).show();
                return false;
            }

            selectedFragment = new TopicFragment(null, null, false, true, publicTopics);
        } else if (item.getItemId() == R.id.menuItemBottomHomepage) {
            selectedFragment = new HomepageFragment(null, null);
        }

        if (selectedFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerView, selectedFragment)
                    .addToBackStack(null)
                    .commit();
            return true;
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_header, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.something_1) {
            // do something
            return true;
        } else if (item.getItemId() == R.id.something_2){
            // do something
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}