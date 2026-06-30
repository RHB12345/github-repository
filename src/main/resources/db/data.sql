INSERT INTO users(student_no, real_name, nickname, phone, email, password_hash, avatar_url, bio, campus, dormitory, role, status) VALUES
('2023000001', '系统管理员', '启航运营', '13800000001', 'admin@aiit.edu.cn', '5cd219c2957410108cf3f1a011d6fa970c80df817414ccb84492cd42a7ef8728', '/images/avatar.svg', '平台管理员，负责商品审核与交易秩序。', '文津校区', '行政楼', 'ADMIN', 'ACTIVE'),
('2023123401', '林一舟', '一舟同学', '13800000002', 'linyizhou@aiit.edu.cn', '5cd219c2957410108cf3f1a011d6fa970c80df817414ccb84492cd42a7ef8728', '/images/avatar.svg', '计算机学院，大三，喜欢把好物传给下一个需要的人。', '文津校区', '松苑 3 栋', 'USER', 'ACTIVE'),
('2023123402', '周若晴', '若晴', '13800000003', 'zhou@aiit.edu.cn', '5cd219c2957410108cf3f1a011d6fa970c80df817414ccb84492cd42a7ef8728', '/images/avatar.svg', '考研党，资料整理控。', '文津校区', '兰苑 2 栋', 'USER', 'ACTIVE'),
('2023123403', '陈知行', '知行', '13800000004', 'chen@aiit.edu.cn', '5cd219c2957410108cf3f1a011d6fa970c80df817414ccb84492cd42a7ef8728', '/images/avatar.svg', '电子产品爱好者，诚信面交。', '新芜校区', '竹苑 5 栋', 'USER', 'ACTIVE'),
('2023123404', '许念', '念念', '13800000005', 'xu@aiit.edu.cn', '5cd219c2957410108cf3f1a011d6fa970c80df817414ccb84492cd42a7ef8728', '/images/avatar.svg', '毕业清仓中，价格好商量。', '校本部', '桂苑 1 栋', 'USER', 'ACTIVE');

INSERT INTO products(seller_id, title, category, price, condition_label, description, status, campus_area, trade_place, view_count, deleted) VALUES
(2, 'Java 编程思想 + Spring Boot 实战套装', '教材资料', 58.00, '八成新', '适合 Java Web 课程和课程设计，重点章节做了标注，送一份自整理面试题 PDF。', 'ON_SALE', '文津校区', '图书馆一楼自习区', 128, FALSE),
(4, '罗技 K380 蓝牙键盘', '数码电子', 89.00, '九成新', '三设备切换正常，键帽干净，适合平板和宿舍桌面使用。', 'ON_SALE', '新芜校区', '竹苑 5 栋楼下', 96, FALSE),
(3, '考研数学高数线代概率资料包', '教材资料', 45.00, '正常使用痕迹', '包含讲义、错题索引和真题年份标注，适合暑假前开始复习。', 'ON_SALE', '文津校区', '兰苑门口', 210, FALSE),
(5, '宿舍可折叠小桌板', '生活用品', 25.00, '八成新', '桌脚稳定，适合床上写作业或者放电脑，毕业搬宿舍出。', 'RESERVED', '校本部', '桂苑 1 栋快递点', 77, FALSE),
(2, '山地自行车 27 速', '运动户外', 380.00, '七成新', '校内通勤很方便，刹车已调，车锁一起送。仅支持面交试骑。', 'ON_SALE', '文津校区', '操场东门', 181, FALSE),
(5, '宜家台灯 + 收纳盒组合', '生活用品', 36.00, '九成新', '灯光柔和，收纳盒适合桌面整理，两个一起带走更划算。', 'ON_SALE', '校本部', '桂苑 1 栋楼下', 64, FALSE),
(4, 'iPad 保护壳和电容笔', '数码电子', 69.00, '八成新', '保护壳适配 10.9 英寸，电容笔书写正常，已完成一笔交易。', 'SOLD', '新芜校区', '教学楼 A 座', 154, FALSE),
(3, '四六级真题与听力训练资料', '教材资料', 22.00, '九成新', '近年真题册和听力训练本，适合冲刺备考。', 'ON_SALE', '文津校区', '图书馆北门', 72, FALSE);

INSERT INTO product_images(product_id, url, sort_no) VALUES
(1, '/images/items/item-01-java-books.jpg', 0),
(2, '/images/items/item-02-keyboard.jpg', 0),
(3, '/images/items/item-03-exam-books.jpg', 0),
(4, '/images/items/item-04-desk-board.jpg', 0),
(5, '/images/items/item-05-bicycle.jpg', 0),
(6, '/images/items/item-06-desk-lamp.jpg', 0),
(7, '/images/items/item-07-ipad-case.jpg', 0),
(8, '/images/items/item-08-english-books.jpg', 0);

INSERT INTO trade_orders(order_no, product_id, buyer_id, seller_id, status, message, completed_at) VALUES
('QH202606290001', 4, 3, 5, 'PENDING', '今晚 8 点后方便在快递点面交吗？', NULL),
('QH202606250001', 7, 2, 4, 'COMPLETED', '已面交，物品状态与描述一致。', CURRENT_TIMESTAMP);

INSERT INTO favorites(user_id, product_id) VALUES
(3, 1),
(4, 1),
(5, 2),
(2, 3),
(3, 5),
(4, 5),
(2, 8);

INSERT INTO comments(product_id, user_id, content) VALUES
(1, 3, '这套书还在吗？能不能只要 Spring Boot 那本？'),
(1, 2, '在的，可以单出，私信我约时间就行。'),
(5, 4, '车架多高？适合 170 左右骑吗？'),
(8, 2, '听力资料有没有答案解析？');

INSERT INTO messages(sender_id, receiver_id, product_id, content, read_flag) VALUES
(3, 2, 1, '同学你好，Java 书今天晚上方便看一下吗？', FALSE),
(2, 3, 1, '可以，图书馆一楼靠窗位置见。', FALSE),
(3, 5, 4, '小桌板我想要，今晚可以取吗？', FALSE),
(5, 3, 4, '可以，我 8 点以后在桂苑快递点。', FALSE);

INSERT INTO users(student_no, real_name, nickname, phone, email, password_hash, avatar_url, bio, campus, dormitory, role, status) VALUES
('2023123405', '韩星澜', '星澜', '13800000006', 'han@aiit.edu.cn', '5cd219c2957410108cf3f1a011d6fa970c80df817414ccb84492cd42a7ef8728', '/images/avatar.svg', '喜欢数码和摄影，交易前可以现场测试。', '文津校区', '竹苑 4 栋', 'USER', 'ACTIVE'),
('2023123406', '顾清野', '清野', '13800000007', 'gu@aiit.edu.cn', '5cd219c2957410108cf3f1a011d6fa970c80df817414ccb84492cd42a7ef8728', '/images/avatar.svg', '运动社团成员，出一些骑行和篮球装备。', '新芜校区', '松苑 2 栋', 'USER', 'ACTIVE'),
('2023123407', '沈禾', '禾子', '13800000008', 'shen@aiit.edu.cn', '5cd219c2957410108cf3f1a011d6fa970c80df817414ccb84492cd42a7ef8728', '/images/avatar.svg', '宿舍收纳爱好者，毕业前整理好物。', '校本部', '兰苑 6 栋', 'USER', 'ACTIVE'),
('2023123408', '叶知夏', '知夏', '13800000009', 'ye@aiit.edu.cn', '5cd219c2957410108cf3f1a011d6fa970c80df817414ccb84492cd42a7ef8728', '/images/avatar.svg', '美妆服饰闲置，支持校内面交验货。', '文津校区', '梅苑 1 栋', 'USER', 'ACTIVE'),
('2023123409', '陆景辰', '景辰', '13800000010', 'lu@aiit.edu.cn', '5cd219c2957410108cf3f1a011d6fa970c80df817414ccb84492cd42a7ef8728', '/images/avatar.svg', '课程设计和竞赛资料很多，欢迎同专业交流。', '新芜校区', '竹苑 1 栋', 'USER', 'ACTIVE');

INSERT INTO products(seller_id, title, category, price, condition_label, description, status, campus_area, trade_place, view_count, deleted) VALUES
(6, 'Sony WH-CH720N 降噪耳机', '数码电子', 299.00, '九成新', '耳机降噪和蓝牙连接正常，盒子和充电线都在，适合图书馆自习和宿舍剪视频。', 'ON_SALE', '文津校区', '图书馆南门', 238, FALSE),
(6, '小米 67W 充电器 + 双 C 线', '数码电子', 55.00, '八成新', '充电器正常使用，线材无破损，适合手机和平板快充。', 'ON_SALE', '文津校区', '竹苑 4 栋楼下', 93, FALSE),
(7, 'Wilson 篮球 7 号', '运动户外', 48.00, '八成新', '球面纹路清楚，气密性正常，社团训练换新球所以出。', 'ON_SALE', '新芜校区', '篮球场东侧', 131, FALSE),
(7, '捷安特骑行头盔', '运动户外', 88.00, '九成新', '尺码 M，佩戴次数少，适合校园骑行和周末短途。', 'ON_SALE', '新芜校区', '校门口驿站', 76, FALSE),
(8, '宿舍桌面洞洞板收纳套装', '生活用品', 42.00, '九成新', '包含挂钩、置物盒和夹子，适合整理耳机线、钥匙和小物件。', 'ON_SALE', '校本部', '兰苑 6 栋楼下', 162, FALSE),
(8, '暖光护眼台灯', '生活用品', 39.00, '八成新', '三档亮度，晚上写作业很舒服，灯罩无破损。', 'ON_SALE', '校本部', '图书馆西门', 118, FALSE),
(9, '日系宽松卫衣 M 码', '美妆服饰', 49.00, '九成新', '厚度适合春秋，颜色偏奶白，已清洗，支持当面看。', 'ON_SALE', '文津校区', '梅苑 1 栋门口', 103, FALSE),
(9, '全新未拆防晒霜 50ml', '美妆服饰', 35.00, '全新', '囤多了出一支，日期新，未拆封，支持面交查看。', 'ON_SALE', '文津校区', '食堂二楼入口', 88, FALSE),
(10, '算法竞赛入门经典 + 蓝桥杯资料', '教材资料', 66.00, '八成新', '包含算法书、蓝桥杯历年题和自己整理的模板笔记，适合竞赛入门。', 'ON_SALE', '新芜校区', '教学楼 B 座大厅', 201, FALSE),
(10, '数据库系统概论教材', '教材资料', 28.00, '七成新', '有少量划线，课程重点章节标注清楚，适合数据库课程复习。', 'ON_SALE', '新芜校区', '竹苑 1 栋快递点', 67, FALSE),
(3, '考研英语真题黄皮书', '教材资料', 38.00, '八成新', '近十年真题，部分年份做过，解析完整，适合暑假开始刷题。', 'ON_SALE', '文津校区', '兰苑 2 栋楼下', 145, FALSE),
(4, '机械键盘 87 键茶轴', '数码电子', 129.00, '八成新', '按键正常，茶轴手感轻，适合宿舍打字和写代码。', 'RESERVED', '新芜校区', '教学楼 A 座', 174, FALSE),
(5, '毕业季行李箱 24 寸', '生活用品', 79.00, '七成新', '轮子顺滑，拉杆正常，有轻微划痕，适合搬寝和短途出行。', 'ON_SALE', '校本部', '桂苑 1 栋楼下', 98, FALSE),
(2, '显示器支架臂', '数码电子', 72.00, '九成新', '桌夹式支架，支持 17-27 寸显示器，宿舍桌面更省空间。', 'ON_SALE', '文津校区', '松苑 3 栋', 119, FALSE);

INSERT INTO product_images(product_id, url, sort_no) VALUES
(9, '/images/items/item-09-headphones.jpg', 0),
(10, '/images/items/item-10-charger.jpg', 0),
(11, '/images/items/item-11-basketball.jpg', 0),
(12, '/images/items/item-12-helmet.jpg', 0),
(13, '/images/items/item-13-organizer.jpg', 0),
(14, '/images/items/item-14-warm-lamp.jpg', 0),
(15, '/images/items/item-15-hoodie.jpg', 0),
(16, '/images/items/item-16-sunscreen.jpg', 0),
(17, '/images/items/item-17-algorithm-books.jpg', 0),
(18, '/images/items/item-18-database-book.jpg', 0),
(19, '/images/items/item-19-english-exam-book.jpg', 0),
(20, '/images/items/item-20-mechanical-keyboard.jpg', 0),
(21, '/images/items/item-21-suitcase.jpg', 0),
(22, '/images/items/item-22-monitor-arm.jpg', 0);

INSERT INTO trade_orders(order_no, product_id, buyer_id, seller_id, status, message, completed_at) VALUES
('QH202606270002', 20, 6, 4, 'PENDING', '键盘还在吗？想今天下午看一下。', NULL),
('QH202606260003', 12, 2, 7, 'COMPLETED', '头盔已面交，状态不错。', CURRENT_TIMESTAMP),
('QH202606240004', 16, 3, 9, 'COMPLETED', '防晒霜未拆封，已完成交易。', CURRENT_TIMESTAMP);

INSERT INTO favorites(user_id, product_id) VALUES
(2, 9),
(3, 9),
(4, 9),
(5, 11),
(6, 17),
(7, 22),
(8, 14),
(9, 13),
(10, 1),
(6, 3),
(7, 5),
(8, 19),
(9, 20),
(10, 9);

INSERT INTO comments(product_id, user_id, content) VALUES
(9, 2, '耳机续航大概还能用多久？'),
(9, 6, '满电正常用两三天没问题，可以现场连手机试。'),
(17, 4, '算法模板笔记可以单独拍一下目录吗？'),
(20, 6, '键盘支持 Mac 吗？'),
(13, 5, '洞洞板尺寸是多少？宿舍桌能放下吗？'),
(22, 3, '支架臂最大承重大概多少？');

INSERT INTO messages(sender_id, receiver_id, product_id, content, read_flag) VALUES
(2, 6, 9, '耳机今天晚上能在图书馆南门看一下吗？', FALSE),
(6, 2, 9, '可以，7 点以后都在。', FALSE),
(4, 10, 17, '蓝桥杯资料还在吗？想看看题目年份。', FALSE),
(10, 4, 17, '在的，晚上我拍目录给你。', FALSE);
