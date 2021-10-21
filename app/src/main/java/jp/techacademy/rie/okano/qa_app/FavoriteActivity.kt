package jp.techacademy.rie.okano.qa_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_favorite.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*

class FavoriteActivity : AppCompatActivity() {

    // --- ここから ---
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mQestionArrayList: ArrayList<Question>
    private lateinit var mAdapter: QuestionsListAdapter
    private var mGenre = 0

    private var mFavoriteRef: DatabaseReference? = null



    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>
            val genre = map["genre"] ?: ""

            mGenre = genre.toInt()

            Log.d("MainActivity", dataSnapshot.key.toString())

            var mQuestionRef: DatabaseReference
            // Firebase
            mDatabaseReference = FirebaseDatabase.getInstance().reference
            mQuestionRef = mDatabaseReference.child(ContentsPATH).child(genre)
            mQuestionRef.addChildEventListener(mQestionEventListener)

        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(p0: DataSnapshot) {

        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onCancelled(p0: DatabaseError) {

        }
    }

    private val mQestionEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot2: DataSnapshot, s: String?) {
            Log.d("MainActivity2", dataSnapshot2.key.toString())
            val map = dataSnapshot2.value as Map<String, String>
            val title = map["title"] ?: ""
            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""
            val imageString = map["image"] ?: ""
            val bytes =
                if (imageString.isNotEmpty()) {
                    Base64.decode(imageString, Base64.DEFAULT)
                } else {
                    byteArrayOf()
                }

            val answerArrayList = ArrayList<Answer>()
            val answerMap = map["answers"] as Map<String, String>?
            if (answerMap != null) {
                for (key in answerMap.keys) {
                    val temp = answerMap[key] as Map<String, String>
                    val answerBody = temp["body"] ?: ""
                    val answerName = temp["name"] ?: ""
                    val answerUid = temp["uid"] ?: ""
                    val answer = Answer(answerBody, answerName, answerUid, key)
                    answerArrayList.add(answer)
                }
            }

            val question = Question(title, body, name, uid, dataSnapshot2.key ?: "",
                mGenre, bytes, answerArrayList)

            Log.d("MainActivity3",dataSnapshot2.key.toString())

            mQestionArrayList.add(question)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(p0: DataSnapshot) {

        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onCancelled(p0: DatabaseError) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)
        title = "お気に入り"

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        // ListViewの準備
        mAdapter = QuestionsListAdapter(this)
        mQestionArrayList = ArrayList<Question>()
        mAdapter.notifyDataSetChanged()

        listView_favorite.setOnItemClickListener { parent, view, position, id ->
            // Questionのインスタンスを渡して質問詳細画面を起動する
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQestionArrayList[position])
            startActivity(intent)
        }
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            mFavoriteRef = mDatabaseReference.child(FavoritePATH).child(user!!.uid)
            mFavoriteRef!!.addChildEventListener(mEventListener)
        }

        // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        mQestionArrayList.clear()
        mAdapter.setQuestionArrayList(mQestionArrayList)
        listView_favorite.adapter = mAdapter

    }
}