const express = require('express');
const mysql = require('mysql2');
const cors = require('cors');

const app = express();
app.use(express.json());
app.use(cors());

// MySQL Database ချိတ်ဆက်ခြင်း (ကိုယ့်ရဲ့ Database အချက်အလက်များဖြင့် ပြင်ရန်)
const db = mysql.createConnection({
    host: 'localhost',
    user: 'root',
    password: 'your_mysql_password',
    database: 'quiz_db'
});

db.connect((err) => {
    if (err) throw err;
    console.log("MySQL Database သို့ အောင်မြင်စွာ ချိတ်ဆက်ပြီးပါပြီ။");
});

// မေးခွန်းများ API ဖြင့် ထုတ်ပေးခြင်း
app.get('/api/questions', (req, res) => {
    let query = "SELECT * FROM questions";
    db.query(query, (err, results) => {
        if (err) return res.status(500).send(err);
        res.json(results);
    });
});

app.listen(3000, () => {
    console.h('Server is running on port 3000');
});