package tomikouten;

///////////////////////////////////////////////////////////////////////////////
//シューティングゲーム８　パワーアップ・複数種類の敵
//ファイル名 : Shooting8.java
//アプレットサイズ 300×400
//使用画像：
//jiki.gif（自機画像上向き、32×32ピクセル）
//tama.gif（弾画像、8×8ピクセル）
//zakoa.gif（ザコ敵Ａ画像下向き、32×32ピクセル）
//zakob.gif（ザコ敵Ｂ画像下向き、32×32ピクセル）
//zakoc.gif（ザコ敵Ｃ画像下向き、32×32ピクセル）
//boss.gif（ボス画像下向き、128×128ピクセル）
//item.gif（アイテム画像、32×32ピクセル）
//使用サウンド：
//shot.au（弾ショット音）
//bang.au（爆発音）
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

///////////////////////////////////////////////////////////////////////////////
// キャラクタクラス

abstract class Chr {
	protected static Shooting8 app; // アプレット
	protected Image img; // 画像
	protected int x, y; // 座標
	protected int w, h; // 幅、高さ
	private boolean dead; // 死亡フラグ

	// コンストラクタ

	// 引数 img:キャラクタ画像のImageオブジェクト
	protected Chr(Image img) {
		// 画像設定
		this.img = img;
		// 画像の幅、高さを取得
		w = img.getWidth(app);
		h = img.getHeight(app);
		// 死亡フラグクリア
		dead = false;
	}

	// 死亡チェック
	boolean isDead() {
		return dead;
	}

	// 死亡する
	void dead() {
		dead = true;
	}

	// 移動
	abstract void move();

	// 当たり判定
	// 引数 t:判定キャラクタ
	// 戻値 当たり:true 外れ:false
	boolean checkHit(Chr t) {
		if (x > t.x - w && x < t.x + t.w && y > t.y - h && y < t.y + t.h) {
			return true;
		}
		return false;
	}

	// 描画
	void draw(Graphics g) {
		g.drawImage(img, x, y, app);
	}
}

///////////////////////////////////////////////////////////////////////////////
// 基本自機クラス

class Jiki extends Chr {

	private int tamaIntCount; // 弾と弾の発射間隔
	private int power; // パワー
	private int jspeed; // スピード
	private int muteki; // 無敵時間
	private int vcount; // 勝利カウント
	private int powert; // パワー保存
	private int victory; // 勝利回数
	// 弾の速度配列
	private static int tv[][] = { { 0, 0 }, { 0, -9 }, { -8, -8, 8, -8, }, { -8, -8, 0, -9, 8, -8 },
			{ -8, -8, 0, -9, 8, -8, -8, 8, 8, 8 }, { 0, -9, 1, -8, 2, -7, 3, -6, 4, -5, 5, -4, 6, -3, 7, -2, 8, -1,

					-9, 0, -8, -1, -7, -2, -6, -3, -5, -4, -4, -5, -3, -6, -2, -7, -1, -8, 0, -9,

					0, 9, -1, 8, -2, 7, -3, 6, -4, 5, -5, 4, -6, 3, -7, 2, -8, 1, -9, 0,

					8, 1, 7, 2, 6, 3, 5, 4, 4, 5, 3, 6, 2, 7, 1, 8, 0, 9, } };

	// コンストラクタ
	Jiki() {
		super(app.imgJiki);
		// 発射間隔をクリア
		tamaIntCount = 0;
		// パワー初期化
		power = 1;
		jspeed = 4;
		muteki = 0;
		vcount = 100;
		victory = 3;
		// 初期座標を設定する
		x = (400 - w) / 2;
		y = 500 - h - 16;
	}

	// 移動
	void move() {
		// 左移動
		if (Key.left) {
			if (Key.shift) {
				x -= 1;
			} else {
				x -= jspeed;
			}
			if (x < 0) {
				x = 0;
			}
		}
		// 右移動
		if (Key.right) {
			if (Key.shift) {
				x += 1;
			} else {
				x += jspeed;
			}
			if (x > 400 - w) {
				x = 400 - w;
			}
		}
		// 上移動
		if (Key.up) {
			if (Key.shift) {
				y -= 1;
			} else {
				y -= jspeed;
			}
			if (y < 0) {
				y = 0;
			}
		}
		// 下移動
		if (Key.down) {
			if (Key.shift) {
				y += 1;
			} else {
				y += jspeed;
			}
			if (y > 500 - h) {
				y = 500 - h;
			}
		}

		// 勝利モード
		if (Key.v && power != 5 && victory > 0) {
			powert = power;
			power = 5;
			victory--;
			app.addVictory(victory);
			muteki = 100;
		}

		// 勝利モード中なら時間を減らす
		if (power == 5) {
			vcount--;
			if (vcount < 0) {
				power = powert;
				vcount = 100;
			}
		}

		// 弾発射
		if (tamaIntCount > 0)
			tamaIntCount--;
		if (tamaIntCount <= 0) {
			for (int i = 0; i < tv[power].length; i += 2) {
				app.addList(new Tama(x + w / 2, y, tv[power][i], tv[power][i + 1]));
			}
			app.sndShot.play();
			tamaIntCount = 6;
		}
		// 無敵中なら無敵時間を減らす
		if (muteki > 0) {
			muteki--;
			if (muteki == 1)
				img = app.imgJiki;
		}

	}

	// 当たり判定
	// 敵とアイテムに当たり判定を行う
	boolean checkHit(Chr t) {
		if ((t instanceof Teki || t instanceof Boss) && muteki == 0) {
			if (super.checkHit(t)) {
				// 敵と当たったら爆発、死亡、ゲームオーバー画面に移行
				img = app.imgJiki_d; // ダメージ描画
				power--;
				app.addPower(power);
				app.sndDame.play();
				app.sndBang.play();
				t.dead();
				muteki = 100;
				if (power < 1) {
					dead();
					app.goOver();
					app.sndSibou.play();
				}
				if (jspeed > 1) {
					jspeed--;
					app.addSpeed(jspeed);
				}
				return true;
			}
		} else if (t instanceof Item) {
			if (super.checkHit(t)) {
				// アイテムに当たったらパワーアップ
				upPower();
				t.dead();
				return true;
			}
		} else if (t instanceof Speed) {
			if (super.checkHit(t)) {
				// アイテムに当たったらパワーアップ(スピードアップ)
				upSpeed();
				t.dead();
				return true;
			}
		}
		return false;
	}

	// パワーアップ
	void upPower() {
		if (power >= 4) {
			// 最大パワーまで達していたらボーナス追加
			app.addScore(1000);
		} else {
			power++;
			app.addPower(power);
		}
	}

	// スピードアップ
	void upSpeed() {
		if (jspeed >= 8) {
			// 最大パワーまで達していたらボーナス追加
			app.addScore(1000);
		} else
			jspeed++;
		app.addSpeed(jspeed);
	}

}
///////////////////////////////////////////////////////////////////////////////
// 背景クラス

class Haikei extends Chr {
	// コンストラクタ
	Haikei(int iy) {
		super(app.imgHaikei);
		x = 0;
		y = iy;

	}

	void move() {
		y += 5;
		if (y > 500)
			y = -495;
	}

	// 当たり判定
	// 誰とも行わない
	boolean checkHit(Chr t) {
		return false;
	}

}
///////////////////////////////////////////////////////////////////////////////
// アイテムクラス

class Item extends Chr {
	// コンストラクタ
	Item() {
		super(app.imgItem);
		// 初期座標を設定する
		x = (int) (Math.random() * (400 - w));
		y = -h;
		app.addList(new Speed(x));
	}

	// 移動
	void move() {
		y += 4;
		if (y > 500)
			dead();
	}

	// 当たり判定
	// 誰とも行わない
	boolean checkHit(Chr t) {
		return false;
	}
}

///////////////////////////////////////////////////////////////////////////////
// スピードクラス

class Speed extends Chr {
	// コンストラクタ
	Speed(int ix) {
		super(app.imgSpeed);
		// 初期座標を設定する
		do {
			x = (int) (Math.random() * (400 - w));
		} while (ix <= x + 100 && ix + 100 >= x);
		y = -h;
	}

	// 移動
	void move() {
		y += 4;
		if (y > 500)
			dead();
	}

	// 当たり判定
	// 誰とも行わない
	boolean checkHit(Chr s) {
		return false;
	}
}

///////////////////////////////////////////////////////////////////////////////
// 敵基本クラス

class Teki extends Chr {
	private int ec = 0; // エネミーセレクト

	// コンストラクタ
	Teki(Image img) {
		super(img);
	}

	// 移動
	void move() {
		// 画面外に出たら死亡
		if (x < -w)
			dead();
		else if (x > 400)
			dead();
		else if (y > 500)
			dead();

		/*

		// bボタンが押されたら爆破、スコア追加、死亡
		if (Key.b) {
			dead();
			app.addList(new Bomm(x, y, ec));
			app.sndBang.play();
			app.addScore(100);
		}

		 */

	}

	// 当たり判定
	// 弾にのみ当たり判定を行う
	boolean checkHit(Chr t) {
		if (t instanceof Tama) {
			if (super.checkHit(t)) {
				// 弾と当たったら爆発、スコア追加、自分と弾死亡
				app.sndBang.play();
				app.addScore(100);
				app.addList(new Bomm(x, y, ec));
				dead();
				t.dead();
				return true;
			}
		}
		return false;
	}

}

///////////////////////////////////////////////////////////////////////////////
// ザコＡクラス まっすぐ突っ込んでくる

class ZakoA extends Teki {
	// コンストラクタ
	ZakoA() {
		super(app.imgZakoA);
		// 初期座標を設定する
		x = (int) (Math.random() * (400 - w));
		y = -h;
	}

	// 移動
	void move() {
		y += 8;
		super.move();
	}
}

///////////////////////////////////////////////////////////////////////////////
// ザコＢクラス 斜めに反射しながら動く

class ZakoB extends Teki {
	private int vx; // 横方向の速度

	// コンストラクタ
	ZakoB() {
		super(app.imgZakoB);
		// 初期座標を設定する
		x = (int) (Math.random() * (400 - w));
		y = -h;
		// 画面左から登場したら右、画面右から登場したら左に動く
		if (x < (400 - w) / 2)
			vx = 8;
		else
			vx = -8;
	}

	// 移動
	void move() {
		y += 4;
		x += vx;
		if (x < 0)
			vx = 8;
		else if (x > 400 - w)
			vx = -8;
		super.move();
	}
}

///////////////////////////////////////////////////////////////////////////////
// ザコＣクラス 自機を追尾する

class ZakoC extends Teki {
	private double vx, vy; // 速度
	private double tv; // 追尾速度
	private int ec = 0; // エネミーセレクト

	// コンストラクタ
	ZakoC() {
		super(app.imgZakoC);
		// 初期座標を設定する
		x = (int) (Math.random() * (400 - w));
		y = -h;
		vx = vy = 0;
		tv = 0.2;
	}

	// 移動
	void move() {
		if (app.getJikix() > x) {
			vx += tv;
			if (vx > 8)
				vx = 8;
		} else if (app.getJikix() < x) {
			vx -= tv;
			if (vx < -8)
				vx = -8;
		}
		if (app.getJikiy() > y) {
			vy += tv;
			if (vy > 8)
				vy = 8;
		} else if (app.getJikiy() < y) {
			vy -= tv;
			if (vy < -8)
				vy = -8;
		}
		x += (int) vx;
		y += (int) vy;

		// 画面外に出たら死亡
		if (x < -w)
			dead();
		else if (x > 360)
			dead();
		else if (y > 500)
			dead();

		/// *

		// bボタンが押されたら爆破、スコア追加、死亡
		if (Key.b) {
			dead();
			app.addList(new Bomm(x, y, ec));
			app.sndBang.play();
			app.addScore(100);
		}

		// */

	}
}
///////////////////////////////////////////////////////////////////////////////
// 敵ボスクラス

class Boss extends Chr {
	protected int ec = 1; // 爆発種類
	protected int vx, vy; // 速度
	protected int rv; // ランダム速度
	protected int power; // 耐久力
	Image imgbosshp = app.imgBosshp30; // ボスHP

	// コンストラクタ
	Boss(Image img) {
		super(img);
	}

	// 移動
	void move() {
		// bボタンが押されたら爆破、スコア追加、死亡

		/// *

		if (Key.b) {
			for (int n = 1; n <= 15; n++) {
				power--;
				if (power < 0) {
					super.dead();
					app.addList(new Bomm(x, y, ec));
				}

				// デバッグ用
				/*
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
				*/
				// デバッグ用
			}
		}

		// */

	}

	// 当たり判定
	// 弾にのみ当たり判定を行う
	boolean checkHit(Chr t) {
		if (t instanceof Tama) {
			if (super.checkHit(t)) {
				// 弾と当たったら爆発、自分と弾死亡
				app.sndBang.play();
				dead();
				t.dead();
				return true;
			}
		}
		return false;
	}

}

///////////////////////////////////////////////////////////////////////////////
// ボスAクラス

class BossA extends Boss {

	// コンストラクタ
	BossA() {
		super(app.imgBoss);
		// 初期座標を設定する
		x = (int) (Math.random() * 300);
		y = -h;
		// ボスの移動速度を取得
		rv = (int) (Math.random() * 6) + 2;
		// 画面左から登場したら右、画面右から登場したら左に動く
		if (x < (400 - w) / 2)
			vx = rv;
		else
			vx = -rv;
		vy = rv;
		power = 30;
	}

	// 移動
	void move() {
		x += vx;
		y += vy;
		if (x < 0)
			vx = rv;
		else if (x > 400 - w)
			vx = -rv;
		if (y < 0)
			vy = rv;
		else if (y > 500 - h)
			vy = -rv;
		app.addList(new Bosshp(x, y, power, imgbosshp));
		super.move();
	}

	// 死亡
	void dead() {
		power--;
		if (power < 20) {
			imgbosshp = app.imgBosshp20;
		}
		if (power < 10) {
			imgbosshp = app.imgBosshp10;
		}
		if (power < 0) {
			super.dead();
			app.addList(new Bomm(x, y, ec));
		}
	}
}
///////////////////////////////////////////////////////////////////////////////
// ボスhpクラス

class Bosshp extends Boss {
	public int power; // 耐久力

	// コンストラクタ
	Bosshp(int xhp, int yhp, int powerhp, Image bosshp) {
		super(bosshp);
		// 初期座標を設定する
		x = xhp;
		y = yhp;
		power = powerhp;
	}

	// 移動
	void move() {
		super.dead();
		super.move();
	}

	// 死亡
	void dead() {
		if (power < 0) {
			super.dead();
		}
	}
}

///////////////////////////////////////////////////////////////////////////////
// 爆発クラス

class Bomm extends Chr {
	private int time = 1; // 爆発が消えるまで時間
	private int time2 = 1; // 爆発を遅延させる
	private int c;

	// コンストラクタ
	// 引数 bx,by:座標
	Bomm(int bx, int by, int ec) {
		super(app.imgBomm[1]);
		c = ec;
		x = bx;
		y = by;
		time = 1;

	}

	// 時間経過で爆発を消す
	void move() {
		if (c == 0) {
			img = app.imgBomm[time];
			if (time >= 8)
				dead();
			time++;
		}
		if (c == 1) {
			img = app.imgEx[time];
			if (time >= 8)
				dead();
			time2++;
			time = time2 / 2;
		}
	}

	// 当たり判定
	// 誰とも行わない
	boolean checkHit(Chr t) {
		return false;
	}
}

///////////////////////////////////////////////////////////////////////////////
// 弾クラス

class Tama extends Chr {
	private int vx, vy; // 速度

	// コンストラクタ
	// 引数 x,y:座標（弾の中心を指定） vx,vy:速度
	Tama(int x, int y, int vx, int vy) {
		super(app.imgTama);
		this.x = x - w / 2;
		this.y = y - h / 2;
		this.vx = vx;
		this.vy = vy;
	}

	// 移動
	void move() {
		x += vx;
		y += vy;
		// 画面外に出たら死亡
		if (x < -w || x > 386 || y < -h || y > 500)
			dead();
	}

	// 当たり判定
	// 誰とも行わない
	boolean checkHit(Chr t) {
		return false;
	}
}

///////////////////////////////////////////////////////////////////////////////
// 画像クラス

abstract class image {
	protected static Shooting8 app; // アプレット
	protected Image img; // 画像
	protected int x, y; // 座標

	// コンストラクタ

	// 引数 img:キャラクタ画像のImageオブジェクト
	protected image(Image img) {
		// 画像設定
		this.img = img;
	}

	// 描画
	void draw(Graphics g) {
		g.drawImage(img, x, y, app);
	}
}

///////////////////////////////////////////////////////////////////////////////
// タイトル背景クラス

class TitleHaikei extends image {
	// コンストラクタ
	TitleHaikei() {
		super(app.imgTitlebgi);
		x = 0;
		y = 0;

	}

}
///////////////////////////////////////////////////////////////////////////////
// タイトロゴクラス

class Trogo extends image {
	// コンストラクタ
	Trogo() {
		super(app.imgtrogo);
		x = 300 - 364 / 2;
		y = 120;

	}

}

///////////////////////////////////////////////////////////////////////////////
// キー入力管理クラス

class Key extends KeyAdapter {
	static boolean left, right, up, down, space, enter, shift, b, v;

	// キーが押されたときの処理
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			left = true;
			break; // ←
		case KeyEvent.VK_RIGHT:
			right = true;
			break; // →
		case KeyEvent.VK_UP:
			up = true;
			break; // ↑
		case KeyEvent.VK_DOWN:
			down = true;
			break; // ↓
		case KeyEvent.VK_SPACE:
			space = true;
			break; // SPACE
		case KeyEvent.VK_ENTER:
			enter = true;
			break; // Enter
		case KeyEvent.VK_SHIFT:
			shift = true;
			break; // Shift
		case KeyEvent.VK_B:
			b = true;
			break; // B
		case KeyEvent.VK_V:
			v = true;
			break; // V
		}
	}

	// キーが離されたときの処理
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			left = false;
			break; // ←
		case KeyEvent.VK_RIGHT:
			right = false;
			break; // →
		case KeyEvent.VK_UP:
			up = false;
			break; // ↑
		case KeyEvent.VK_DOWN:
			down = false;
			break; // ↓
		case KeyEvent.VK_SPACE:
			space = false;
			break; // SPACE
		case KeyEvent.VK_ENTER:
			enter = false;
			break; // Enter
		case KeyEvent.VK_SHIFT:
			shift = false;
			break; // Shift
		case KeyEvent.VK_B:
			b = false;
			break; // B
		case KeyEvent.VK_V:
			v = false;
			break; // V
		}
	}
}

///////////////////////////////////////////////////////////////////////////////
// メイン

public class Shooting8 extends Applet implements Runnable {
	private Vector clist, clistTmp; // キャラクタリスト・仮キャラクタリスト
	private volatile Thread gameThread; // ゲームスレッド
	private Image offImage; // 仮想画面
	private Graphics gv; // 仮想画面Graphicsオブジェクト
	private final int SCENE_INIT = 0; // シーン：初期化
	private final int SCENE_TITLE = 1; // シーン：タイトル
	private final int SCENE_MAIN = 2; // シーン：メイン
	private final int SCENE_OVER = 3; // シーン：ゲームオーバー
	private final int SCENE_RANK = 4; // シーン：ランキング
	private int scene; // シーン
	private int score; // 得点
	private Font scoreFont; // 得点用フォント
	private int width, height; // アプレットの幅、高さ
	private MediaTracker mt; // 画像読み込み用メディアトラッカー
	private int tekiInterval; // 敵出現間隔
	private int tekiIntCount; // 敵出現間隔カウンタ
	private int itemIntCount; // アイテム出現間隔カウンタ
	private Jiki jiki; // 自機オブジェクト保存用
	private TitleHaikei hai; // タイトル背景オブジェクト保存用
	private Trogo trogo; // タイトルロゴオブジェクト保存用
	private int score_a = 0; // スコア更新の制限
	private int power_out; // パワー（表示用）
	private int speed_out; // スピード（表示用）
	private int victory_out; // ビクトリー（表示用）
	private int title = 0; // タイトルセレクト
	Image imgJiki, imgItem, imgSpeed, imgTama, imgJiki_d; // 各イメージ（自機、アイテム、弾、自機ダメージ,影）
	Image imgHaikei, imgTitlebgi; // 背景のイメージ (メインループ、タイトル)
	Image[] imgBomm = new Image[9]; // 爆発のイメージ
	Image[] imgEx = new Image[9]; // ボス爆発のイメージ
	Image imgZakoA, imgZakoB, imgZakoC; // 各イメージ（ザコＡ、ザコＢ、ザコＣ）
	Image imgBoss, imgBosshp30, imgBosshp20, imgBosshp10; // 各イメージ（ボス）
	Image imgtrogo; // タイトルロゴ
	AudioClip sndShot, sndBang, sndDame, sndSibou; // 各サウンド（ショット、爆発）
	Image buff; // ダブルバッファリング用のバッファ
	Graphics buff_g; // ダブルバッファリング用のグラフィックス
	private int bosspop; // ボスの出現頻度
	private int bosspop1; // ボスの初期出現

	// 初期化
	public void init() {
		width = getSize().width;
		height = getSize().height;
		scene = SCENE_INIT;
		// 得点用フォント生成
		scoreFont = new Font("Arial", Font.BOLD, 20);
		// 背景色を黒色に設定
		setBackground(Color.DARK_GRAY);
		// 前景色を白色に設定
		setForeground(Color.green);
		// 仮想画面の生成
		offImage = createImage(width, height);
		gv = offImage.getGraphics();
		// キー受け付けオブジェクト生成
		addKeyListener(new Key());
		// Chrクラスにアプレットを渡す
		Chr.app = this;
		image.app = this;
		// メディアトラッカー生成
		mt = new MediaTracker(this);
		// キャラクタリスト・仮キャラクタリスト生成
		clist = new Vector();
		clistTmp = new Vector();
		// 画像読み込み・メディアトラッカーに登録
		imgJiki = getImage(getDocumentBase(), "jiki.gif"); // 自機
		mt.addImage(imgJiki, 0);
		imgJiki_d = getImage(getDocumentBase(), "jiki_d.gif"); // 自機ダメ―ジ
		mt.addImage(imgJiki_d, 0);
		imgItem = getImage(getDocumentBase(), "item.gif"); // アイテム
		mt.addImage(imgItem, 0);
		imgSpeed = getImage(getDocumentBase(), "speed.gif"); // アイテム
		mt.addImage(imgSpeed, 0);
		imgZakoA = getImage(getDocumentBase(), "zakoa.gif"); // ザコＡ
		mt.addImage(imgZakoA, 0);
		imgZakoB = getImage(getDocumentBase(), "zakob.gif"); // ザコＢ
		mt.addImage(imgZakoB, 0);
		imgZakoC = getImage(getDocumentBase(), "zakoc.gif"); // ザコＣ
		mt.addImage(imgZakoC, 0);
		imgBoss = getImage(getDocumentBase(), "boss.gif"); // ボス
		mt.addImage(imgBoss, 0);
		imgBosshp30 = getImage(getDocumentBase(), "bosshp30.png"); // ボスHP30
		mt.addImage(imgBosshp30, 0);
		imgBosshp20 = getImage(getDocumentBase(), "bosshp20.png"); // ボスHP20
		mt.addImage(imgBosshp20, 0);
		imgBosshp10 = getImage(getDocumentBase(), "bosshp10.png"); // ボスHP10
		mt.addImage(imgBosshp10, 0);
		imgTama = getImage(getDocumentBase(), "tama.gif"); // 弾
		mt.addImage(imgTama, 0);
		imgHaikei = getImage(getDocumentBase(), "haikei.png"); // メイン背景
		mt.addImage(imgHaikei, 0);
		imgTitlebgi = getImage(getDocumentBase(), "Title_bgi.jpg"); // タイトル背景
		mt.addImage(imgTitlebgi, 0);
		imgBomm[1] = getImage(getDocumentBase(), "bomm01.gif"); // 爆発1
		mt.addImage(imgBomm[1], 0);
		imgBomm[2] = getImage(getDocumentBase(), "bomm02.gif"); // 爆発2
		mt.addImage(imgBomm[2], 0);
		imgBomm[3] = getImage(getDocumentBase(), "bomm03.gif"); // 爆発3
		mt.addImage(imgBomm[3], 0);
		imgBomm[4] = getImage(getDocumentBase(), "bomm04.gif"); // 爆発4
		mt.addImage(imgBomm[4], 0);
		imgBomm[5] = getImage(getDocumentBase(), "bomm05.gif"); // 爆発5
		mt.addImage(imgBomm[5], 0);
		imgBomm[6] = getImage(getDocumentBase(), "bomm06.gif"); // 爆発6
		mt.addImage(imgBomm[6], 0);
		imgBomm[7] = getImage(getDocumentBase(), "bomm07.gif"); // 爆発7
		mt.addImage(imgBomm[7], 0);
		imgBomm[8] = getImage(getDocumentBase(), "bomm08.gif"); // 爆発8
		mt.addImage(imgBomm[8], 0);
		imgEx[1] = getImage(getDocumentBase(), "Explosion01.gif"); // ボス爆発1
		mt.addImage(imgEx[1], 0);
		imgEx[2] = getImage(getDocumentBase(), "Explosion02.gif"); // ボス爆発2
		mt.addImage(imgEx[2], 0);
		imgEx[3] = getImage(getDocumentBase(), "Explosion03.gif"); // ボス爆発3
		mt.addImage(imgEx[3], 0);
		imgEx[4] = getImage(getDocumentBase(), "Explosion04.gif"); // ボス爆発4
		mt.addImage(imgEx[4], 0);
		imgEx[5] = getImage(getDocumentBase(), "Explosion05.gif"); // ボス爆発5
		mt.addImage(imgEx[5], 0);
		imgEx[6] = getImage(getDocumentBase(), "Explosion06.gif"); // ボス爆発6
		mt.addImage(imgEx[6], 0);
		imgEx[7] = getImage(getDocumentBase(), "Explosion07.gif"); // ボス爆発7
		mt.addImage(imgEx[7], 0);
		imgEx[8] = getImage(getDocumentBase(), "Explosion08.gif"); // ボス爆発8
		mt.addImage(imgEx[8], 0);
		imgtrogo = getImage(getDocumentBase(), "TitleRogo.png"); // タイトルロゴ
		mt.addImage(imgtrogo, 0);
		// サウンド読み込み
		// 8000Hz,mono,8bit,μ-lawのSun形式(.au)
		sndShot = getAudioClip(getDocumentBase(), "shot.au"); // 弾ショット
		sndBang = getAudioClip(getDocumentBase(), "bang.au"); // 爆発
		sndDame = getAudioClip(getDocumentBase(), "dame.au"); // 爆発
		sndSibou = getAudioClip(getDocumentBase(), "sibou.mp3"); // 爆発
		// フォーカス要求
		requestFocus();
		resize(600, 500);
		// ダブルバッファリング用
		buff = createImage(600, 500);
		buff_g = buff.getGraphics();
	}

	// ゲームスレッドの開始
	public void start() {
		if (gameThread == null) {
			gameThread = new Thread(this);
			gameThread.start();
		}
	}

	// ゲームスレッドの停止
	public void stop() {
		gameThread = null;
	}

	// ゲームスレッドのメイン
	public void run() {
		while (gameThread == Thread.currentThread()) {
			// 裏画面の消去
			gv.clearRect(0, 0, width, height);
			// 各シーン処理
			switch (scene) {
			case SCENE_INIT:
				gameInit();
				break;
			case SCENE_TITLE:
				gameTitle();
				break;
			case SCENE_MAIN:
				gameMain();
				break;
			case SCENE_OVER:
				gameOver();
				break;
			case SCENE_RANK:
				gameRank();
				break;
			}
			// 再描画
			repaint();
			// 20ミリ秒待つ
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				break;
			}

		}
	}

	// ゲーム初期化処理
	private void gameInit() {
		// 画面描画
		drawCenter("Loading...", 180);
		// 画像が完全に読み込まれるまで待つ
		if (mt.statusAll(true) == MediaTracker.COMPLETE) {
			// 準備処理
			ready();
			// タイトル画面へ移行
			scene = SCENE_TITLE;
		}
	}

	// 準備処理（ゲーム開始するたびに呼ばれる）
	private void ready() {
		// 得点クリア
		score = 0;
		score_a = 0;
		// パワーの初期化
		power_out = 1;
		// スピードの初期化
		speed_out = 4;
		// ビクトリーの初期化
		victory_out = 3;
		// 出現間隔を初期値に
		tekiInterval = 50;
		tekiIntCount = tekiInterval;
		itemIntCount = 0;
		// キャラクタリストクリア
		clist.setSize(0);
		// テンポラリキャラクタリストクリア
		clistTmp.setSize(0);
		// 背景オブジェクトの追加
		addList(new Haikei(-500));
		addList(new Haikei(0));
		hai = new TitleHaikei();
		trogo = new Trogo();
		// 自機オブジェクト追加
		jiki = new Jiki();
		addList(jiki);
		// ボス出現頻度の初期化
		bosspop = 0;
		// ボスの初期出現の初期化
		bosspop1 = 0;

	}

	// ゲームタイトル処理
	private void gameTitle() {

		// 20ミリ秒待つ
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
		}
		hai.draw(gv);
		if (Key.up)
			title = 0;
		if (Key.down)
			title = 1;
		// エンターキーが押されたとき
		if (Key.enter) {
			if (title == 0) {
				// ガベージコレクションを行う
				System.gc();
				// ゲームメインへ移行
				scene = SCENE_MAIN;
			} else {
				scene = SCENE_RANK;
			}
		}
		// タイトル画面描画
		// gv.setFont(new Font("Arial", Font.BOLD, 40));
		// drawCenter("G. SHOOTING !", 180);
		trogo.draw(gv);

		if (title == 0) {
			gv.setFont(new Font("Arial", Font.BOLD, 30));
			drawCenter("GAME STERT", 350);
			gv.setFont(new Font("Arial", Font.PLAIN, 20));
			drawCenter("RANKINNG", 400);
		} else {
			gv.setFont(new Font("Arial", Font.BOLD, 30));
			drawCenter("RANKINNG", 400);
			gv.setFont(new Font("Arial", Font.PLAIN, 20));
			drawCenter("GAME STERT", 350);

		}

	}

	// ゲームメイン処理
	private void gameMain() {
		int i, j;
		Chr c, c2;
		image im;
		// 移動
		for (i = 0; i < clist.size(); i++) {
			c = (Chr) (clist.elementAt(i));
			c.move();
		}
		// 描画
		for (i = 0; i < clist.size(); i++) {
			c = (Chr) (clist.elementAt(i));
			c.draw(gv);
		}
		// 当たり判定
		for (i = 0; i < clist.size(); i++) {
			c = (Chr) (clist.elementAt(i));
			for (j = 0; j < clist.size(); j++) {
				c2 = (Chr) (clist.elementAt(j));
				c.checkHit(c2);
			}
		}
		// 死亡チェック
		i = 0;
		while (i < clist.size()) {
			c = (Chr) (clist.elementAt(i));
			// 死亡していたらリストから削除
			if (c.isDead())
				clist.removeElementAt(i);
			else
				i++;
		}
		// ボスの出現頻度の設定
		if (score >= 80000)
			bosspop = 20;
		else if (score >= 50000)
			bosspop = 30;
		else if (score >= 20000)
			bosspop = 40;
		else if (score >= 5000) {
			bosspop = 50;
			if (bosspop1 == 0)
				addList(new BossA());
			bosspop1 = 50;
		}
		// 敵出現
		tekiIntCount--;
		if (tekiIntCount <= 0) {
			int kind = (int) (Math.random() * 3);
			if (kind == 0)
				addList(new ZakoA());
			if (kind == 1)
				addList(new ZakoB());
			if (kind == 2)
				addList(new ZakoC());
			if (score >= 10000)
				if ((int) (Math.random() * bosspop) == 1) {
					addList(new BossA());
				}
			tekiInterval--;
			if (tekiInterval < 1)
				tekiInterval = 20;
			tekiIntCount = tekiInterval;
		}
		// アイテム出現
		itemIntCount++;
		if (itemIntCount > 300) {
			addList(new Item());
			itemIntCount = 0;
		}
		// キャラクタリストにテンポラリのキャラクタを追加
		for (i = 0; i < clistTmp.size(); i++) {
			clist.addElement(clistTmp.elementAt(i));
		}
		clistTmp.setSize(0);

		// スコアの表示
		gv.setFont(scoreFont);
		gv.drawString("SCORE : " + String.valueOf(score), 420, 30);
		gv.drawString("POWER : " + String.valueOf(power_out) + " / 4", 420, 60);
		gv.drawString("SPEED : " + String.valueOf(speed_out) + " / 8", 420, 90);
		gv.drawString("VICTORY : " + String.valueOf(victory_out) + " / 3", 420, 120);

	}

	// ゲームオーバー処理
	private void gameOver() {
		hai.draw(gv);
		// エンターキーが押されたら
		if (Key.enter) {
			// ゲーム準備処理を行い
			// ready();
			// ランキング画面へ移行
			scene = SCENE_RANK;
		}
		// ゲームオーバー画面描画
		gv.setFont(new Font("Arial", Font.BOLD, 50));
		drawCenter("GAME OVER", 180);
		gv.setFont(new Font("Arial", Font.BOLD, 30));
		drawCenter("SCORE : " + String.valueOf(score), 350);
	}

	// ランキング処理
	private void gameRank() {
		int t = 60; // 文字のｙ座標
		int work;
		int r[] = new int[6]; // ランキング(整数)
		String rank[] = new String[6]; // ランキング(文字列)

		hai.draw(gv);
		// ファイルの読み込み
		try {
			File file = new File("rank.txt");
			BufferedReader filereader = new BufferedReader(new FileReader(file));

			// スコアの取得
			if (score_a == 0) {
				r[5] = score;
				score_a = 1;
			}

			// 並び替え・出力
			for (int b = 0; b < 5; b++) {
				rank[b] = filereader.readLine();
			}
			for (int b = 0; b < 5; b++) {
				r[b] = Integer.parseInt(rank[b]);
			}
			for (int c = 0; c <= 5; c++) {
				for (int b = 0; b < 5; b++) {
					if (r[b] <= r[b + 1]) {
						work = r[b + 1];
						r[b + 1] = r[b];
						r[b] = work;

					}
				}
			}
			for (int b = 0; b < 5; b++) {
				gv.setFont(new Font("Arial", Font.BOLD, 28));
				rank[b] = String.valueOf(r[b]);
				if (r[b] == score && b != 0) {
					drawCenter("NEW SCORE", 380);
					gv.setFont(new Font("Arial", Font.BOLD, 40));
				}
				if (r[b] == score && b == 0) {
					gv.setFont(new Font("Arial", Font.BOLD, 40));
					drawCenter("BEST SCORE", 380);
				}

				drawCenter("RANK " + (b + 1) + "      " + rank[b], t);
				t += 50;
			}

			// ファイルを閉じる
			filereader.close();

			PrintWriter pw = new PrintWriter(file);
			pw.print(score);

			pw.close();

		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}

		// ファイルに書き込み
		try {
			File file = new File("rank.txt");

			PrintWriter pw = new PrintWriter(file);
			for (int a = 0; a <= 4; a++) {
				pw.print(rank[a]);
				pw.print("\n");
			}

			pw.close();

		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}

		// 50ミリ秒待つ
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
		}

		// エンターキーが押されたら
		if (Key.enter) {
			// ゲーム準備処理を行い
			ready();
			// ランキング画面へ移行
			scene = SCENE_TITLE;
		}
	}

	// アプレットの幅、高さを得る
	int getw() {
		return width;
	}

	int geth() {
		return height;
	}

	// 自機の座標を取得
	int getJikix() {
		return jiki.x;
	}

	int getJikiy() {
		return jiki.y;
	}

	// 文字列を中央揃えで表示
	// 引数 str:文字列、y:y座標
	private void drawCenter(String str, int y) {
		FontMetrics fm = getFontMetrics(gv.getFont());
		gv.drawString(str, (width - fm.stringWidth(str)) / 2, y);
	}

	// テンポラリキャラクタリストにキャラクタを追加する
	void addList(Chr c) {
		clistTmp.addElement(c);
	}

	// ゲームオーバーに移行
	void goOver() {
		scene = SCENE_OVER;
	}

	// スコア追加
	void addScore(int s) {
		score += s;
	}

	// パワーの取得
	void addPower(int p) {
		power_out = p;
	}

	// スピードの取得
	void addSpeed(int s) {
		speed_out = s;
	}

	// ビクトリーの取得
	void addVictory(int v) {
		victory_out = v;
	}

	public void drawImage(Graphics g) {
		// 裏画面から表画面へ転送
		g.drawImage(offImage, 0, 0, this);

	}

	// 描画
	public void paint(Graphics g) {
		buff_g.setColor(Color.black);
		buff_g.fillRect(0, 0, 1000, 800);
		drawImage(buff_g);
		g.drawImage(buff, 0, 0, this);
	}

	// 更新
	public void update(Graphics g) {
		paint(g);
	}
}
