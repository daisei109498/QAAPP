package jp.techacademy.rie.okano.qa_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.list_questions.view.*

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var mFavoriteRef: DatabaseReference
    private var mGenre: Int = 0

    private val mEventListener = object : ChildEventListener {

        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] as? String ?: ""
            val name = map["name"] as? String ?: ""
            val uid = map["uid"] as? String ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    private val mFavoriteEventListener =  object : ValueEventListener {

        override fun onDataChange(snapshot: DataSnapshot) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                // ログインしていなければお気に入り非表示
                fab2.visibility = View.INVISIBLE
                fab3.visibility = View.INVISIBLE
            }else{
                if (snapshot.value == null) {
                    Log.d("MainActivity", "no")
                    fab3.visibility = View.INVISIBLE
                    fab2.visibility = View.VISIBLE
                } else {
                    Log.d("MainActivity", snapshot.value.toString()+"yes")
                    fab2.visibility = View.INVISIBLE
                    fab3.visibility = View.VISIBLE
                }
            }

        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.d("MainActivity", "onCancelled")
        }
    }

    override fun onResume() {
        super.onResume()
        val dataBaseReference = FirebaseDatabase.getInstance().reference
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // ログインしていなければお気に入り非表示
            fab2.visibility = View.INVISIBLE
            fab3.visibility = View.INVISIBLE
        }else {
            mFavoriteRef =
                dataBaseReference.child(FavoritePATH).child(user!!.uid).child(mQuestion.questionUid)
            mFavoriteRef.addValueEventListener(mFavoriteEventListener)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras!!.get("question") as Question

        mGenre = extras!!.getInt("genre")

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                // --- ここから ---
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
                // --- ここまで ---
            }
        }

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            mFavoriteRef =
                dataBaseReference.child(FavoritePATH).child(user!!.uid).child(mQuestion.questionUid)
            mFavoriteRef.addValueEventListener(mFavoriteEventListener)
        }

        fab2.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                        var data = HashMap<String,String>()
                        data["genre"]="1"
                        mFavoriteRef.push().setValue(data)
            }
        }

        fab3.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                mFavoriteRef.removeValue()
            }

        }

    }
}