package com.example.sandyl.todoapp_materialdesign;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.sandyl.todoapp_materialdesign.Todo.Status.ACTIVE;
import static com.example.sandyl.todoapp_materialdesign.Todo.Status.DONE;

public class MainActivity extends AppCompatActivity implements RecyclerView.OnItemTouchListener {

    private String TAG;
    private Toolbar mToolbar;
    RecyclerView recyclerView;
    List<Todo> todos;
    CustomAdapter adapter;
    private FloatingActionButton fab;
    TextView dateTextView;

    //database helper
    TodoDatabaseAdapter todoDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //database initialization
        todoDatabase = new TodoDatabaseAdapter(this);
        // SQLiteDatabase db =  todoDatabase.todoDatabaseHelper.getWritableDatabase();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.rvItems);
        todos = new ArrayList<Todo>();

        displayData();

        adapter = new CustomAdapter(MainActivity.this, todos);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));


        AddTodoAction();
        setRecyclerViewClickListener();

    }

    public void setRecyclerViewClickListener() {
        recyclerView = (RecyclerView) findViewById(R.id.rvItems);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(MainActivity.this, recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        // do whatever

                        Todo todoSelected  = todos.get(position);
                        Intent intent = new Intent(MainActivity.this, EditTodoActivity.class);
                        intent.putExtra("uid", todoSelected.getId());
                        intent.putExtra("task", todoSelected.getText());
                        intent.putExtra("priority",   putPriority(todoSelected.getPriority()));
                        intent.putExtra("status", putStatus(todoSelected.getStatus()));
                        intent.putExtra("date",getDateStr(todoSelected.getDate()));
                        intent.putExtra("position", position);
                        startActivityForResult(intent, 2);

                        Log.d("TAG", "id of todo selected" + todoSelected.getId());


                        getTodo(todoSelected);
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public  List displayData() {

        List<Todo> data = new ArrayList<Todo>();

        if (todoDatabase.getTodosCount() > 0) {
           data = queryAll();
        } else {
            data = getData();
        }

        return  data;
    }

    public List getData() {

        List<Todo> data;

        List<String> tasks = new ArrayList<String>();
        tasks.add("Task 1");
        tasks.add("Task 2");
        tasks.add("Task 3");
        tasks.add("Task 4");

        data =  new ArrayList<Todo>();


        for (int i = 0; i < tasks.size() ; i++) {

            Todo todo = new Todo();
            todo.text = tasks.get(i);
            todo.date = new Date();
            todo.priority = Todo.Priority.LOW;
            todo.status = Todo.Status.ACTIVE;

            Log.d("TAG", todo.text);
            data.add(todo);
        }

        return data;
    }


    public void addTodo(Todo todo) {

        Todo newTodo = new Todo();
        newTodo.text = todo.text;
        newTodo.date = todo.date;
        newTodo.status = todo.status;
        newTodo.priority = todo.priority;

        todos.add(newTodo);

        todoDatabase.insertTodo(newTodo);

        adapter.notifyDataSetChanged();

    }


    public void updateTodo(Todo todo) {

        todoDatabase.updateTodo(todo);


        adapter.notifyDataSetChanged();

    }



    public void deleteTodo(Todo todo) {

        todoDatabase.deleteTodo(todo);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "todo deleted: " +todo.getText()+" ("+todo.getId()+") " , Toast.LENGTH_LONG).show();

    }



    public List queryAll() {
        todos.addAll(todoDatabase.getAllData());

        return  todos;
    }

    public void getTodo(Todo todo) {

        Todo aTodo = new Todo();
        aTodo = todoDatabase.getTodo(todo);

        Toast.makeText(this, "todo selected is " +aTodo.getText()+" ("+aTodo.getId()+") " , Toast.LENGTH_LONG).show();
    }

    public void AddTodoAction() {

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.turquoise)));

        //get the drawable
        Drawable myFabSrc = ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_input_add, getTheme());

        Drawable whiteFab = myFabSrc.getConstantState().newDrawable();

        whiteFab.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);

        fab.setImageDrawable(whiteFab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddTodoActivity.class);
                startActivityForResult(intent, 1);
            }
        });

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        String text = data.getStringExtra("task");
        String priority = data.getStringExtra("priority");
        String date = data.getStringExtra("date");
        String status = data.getStringExtra("status");
        int position = data.getIntExtra("position", -1);


        Todo todo = new Todo();
        todo.setText(text);
        todo.setDate(getDate(date));
        todo.setStatus(setTodoStatus(status));
        todo.setPriority(setTodoPriority(priority));

        if (resultCode == 1) {
            Log.d("TAG", "save");
            int uid = todos.size()+1;
            todo.setId(uid);
            addTodo(todo);

            Toast.makeText(this, "todo added: " +todo.getText()+" ("+todo.getId()+") " , Toast.LENGTH_LONG).show();
        }

        if (resultCode == 2) {
            Log.d("TAG", "edit");
            todos.remove(position);
            todos.add(position, todo);
            int uid = data.getIntExtra("uid", -1);

            todo.setId(uid);
            updateTodo(todo);

        }

        if (resultCode == 3){

            todos.remove(position);

            int uid = data.getIntExtra("uid", -1);
            todo.setId(uid);
            deleteTodo(todo);

        }

        Log.d("TAG", "todo saved: " + priority);
        Log.d("TAG", "todo saved: " + status);

    }

    public Todo.Priority setTodoPriority(String priority) {

        //to initialize
        Todo.Priority priorityLevel = Todo.Priority.LOW;

        switch (priority.toLowerCase()) {
            case "low":
                priorityLevel = Todo.Priority.LOW;
                break;

            case "medium":
                priorityLevel = Todo.Priority.MEDIUM;
                break;

            case "high":
                priorityLevel = Todo.Priority.HIGH;
                break;

        }

        return  priorityLevel;
    }

    public String putPriority(Todo.Priority  priority) {

        //to initialize
        String priorityLevel = "";

        switch (priority) {
            case LOW:
                priorityLevel = "low";
                break;

            case MEDIUM:
                priorityLevel = "medium";
                break;

            case HIGH:
                priorityLevel = "high";
                break;

        }
        return  priorityLevel;
    }




    public Todo.Status setTodoStatus(String status) {

        //to initialize
        Todo.Status todoStatus = ACTIVE;

        switch (status.toLowerCase()) {
            case "active":
                todoStatus = ACTIVE;
                break;

            case "done":
                todoStatus = DONE;
                break;

        }

        return  todoStatus;
    }

    public String putStatus(Todo.Status  status) {

        //to initialize
        String todoStatus = new String();

        switch (status) {
            case ACTIVE:
                todoStatus = "active";
                break;

            case DONE:
                todoStatus= "done";
                break;

        }
        return  todoStatus;
    }


    public Date getDate(String dateStr) {

        DateFormat dateFormat ;
        Date date = new Date();
        dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        try
        {
            date = (Date) dateFormat.parse(dateStr);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return date;

    }

    public String getDateStr(Date date) {

        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

        String stringDate = formatter.format(date);

        return  stringDate;
    }

}
