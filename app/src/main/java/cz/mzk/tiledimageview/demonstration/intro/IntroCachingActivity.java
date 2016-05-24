package cz.mzk.tiledimageview.demonstration.intro;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.mzk.tiledimageview.TiledImageView;
import cz.mzk.tiledimageview.demonstration.R;
import cz.mzk.tiledimageview.images.TiledImageProtocol;

/**
 * Created by Martin Řehánek on 21.5.16.
 */
public class IntroCachingActivity extends AppCompatActivity implements TiledImageView.MetadataInitializationListener, View.OnClickListener {

    private static final String TAG = IntroCachingActivity.class.getSimpleName();
    private static final String EXTRA_CURRENT_POSITION = "current_position";
    private final List<String> mBaseUrlList = initBaseUrlList();
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.tiledImageView) TiledImageView mImageView;
    @BindView(R.id.btnPreviousPage) Button mBtnPrevious;
    @BindView(R.id.btnNextPage) Button mBtnNext;
    @BindView(R.id.pageCounter) TextView mPageCounter;
    private int mCurrentPagePosition = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_caching);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mBtnPrevious.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
        mImageView.setMetadataInitializationListener(this);
        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_CURRENT_POSITION)) {
            mCurrentPagePosition = savedInstanceState.getInt(EXTRA_CURRENT_POSITION);
        }
        updateViews();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(EXTRA_CURRENT_POSITION, mCurrentPagePosition);
        super.onSaveInstanceState(outState);
    }

    private void updateViews() {
        Log.v(TAG, "showing image");
        mImageView.setVisibility(View.INVISIBLE);
        String baseUrl = mBaseUrlList.get(mCurrentPagePosition);
        mImageView.loadImage(TiledImageProtocol.ZOOMIFY, baseUrl);
        //buttons and counter
        mPageCounter.setText("" + (mCurrentPagePosition + 1) + " / " + mBaseUrlList.size());
        mBtnPrevious.setEnabled(mCurrentPagePosition > 0);
        mBtnNext.setEnabled(mCurrentPagePosition + 1 < mBaseUrlList.size());
    }

    @Override
    public void onMetadataInitialized() {
        mImageView.setVisibility(View.VISIBLE);
        mImageView.requestLayout();
    }

    private void toastAndFinish(String message) {
        Toast.makeText(this, "error fetching metadata: " + message, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onMetadataUnhandableResponseCode(String imageMetadataUrl, int responseCode) {
        toastAndFinish("unhandable response code");
    }

    @Override
    public void onMetadataRedirectionLoop(String imageMetadataUrl, int redirections) {
        toastAndFinish("redirection loop");
    }

    @Override
    public void onMetadataDataTransferError(String imageMetadataUrl, String errorMessage) {
        toastAndFinish("transfer error");
    }

    @Override
    public void onMetadataInvalidData(String imageMetadataUrl, String errorMessage) {
        toastAndFinish("invalid data");
    }

    @Override
    public void onCannotExecuteMetadataInitialization(String imageMetadataUrl) {
        toastAndFinish("cannot execute metadata initialization");
    }

    @Override
    public void onClick(View v) {
        if (v == mBtnPrevious) {
            mCurrentPagePosition--;
            updateViews();
        } else if (v == mBtnNext) {
            mCurrentPagePosition++;
            updateViews();
        }
    }

    private List<String> initBaseUrlList() {
        List<String> list = new ArrayList<>();
        String prefix = "http://kramerius.mzk.cz/search/zoomify/";
        list.add(prefix + "uuid:d611a9c9-2c98-485c-acd3-1584d9605363");
        list.add(prefix + "uuid:ff61b15b-e241-4cc2-b749-72a5938e3d04");
        list.add(prefix + "uuid:9d3590ec-b848-48e8-ac7d-14181976d587");
        list.add(prefix + "uuid:a6d55f38-d94b-4e28-80a8-213b42e67003");
        list.add(prefix + "uuid:c986217b-fe7a-411b-9002-fae95afaf464");
        list.add(prefix + "uuid:87c1333f-ba98-4a3e-a4a4-73a63300d71b");
        list.add(prefix + "uuid:b11a4d9e-0fba-410f-a12f-2ea5fa05b918");
        list.add(prefix + "uuid:816a21d7-39c1-4899-91ab-34cc7610aa68");
        list.add(prefix + "uuid:2441a761-ecc3-45ed-b6e0-d7ec7b13aaca");
        list.add(prefix + "uuid:0bc88675-122b-4547-89cc-86ff2e4ada02");
        list.add(prefix + "uuid:1741c909-81fe-4b7a-8845-63c1d98e8d71");
        list.add(prefix + "uuid:e8d9bcc8-e5a1-479e-a78f-7886296e34bf");
        list.add(prefix + "uuid:8fc397f3-d8df-479c-ad6c-3a0900ec5ca8");
        list.add(prefix + "uuid:4b477a2a-3ced-4859-a172-ab7bbe18a21c");
        list.add(prefix + "uuid:239c95b4-690e-404e-9d20-5466802ad19a");
        list.add(prefix + "uuid:e6d4dfb0-8b94-4cc2-a84e-728c5005fdb8");
        list.add(prefix + "uuid:f89264df-41d7-4231-bbf9-722333f9f32a");
        list.add(prefix + "uuid:637598e7-56e0-416a-99b5-7503713b8ec0");
        list.add(prefix + "uuid:a70bde28-3a07-4f37-87a7-89c6c05dff71");
        list.add(prefix + "uuid:b6de3f9d-6103-4e3a-aac0-13c5cfa1e950");
        list.add(prefix + "uuid:c850e4a0-d73e-4a34-9360-6d450c77b2fd");
        list.add(prefix + "uuid:e3a2171e-f556-4d2e-ad87-d6577d1d6444");
        list.add(prefix + "uuid:ca01de34-3e82-4cce-8ca1-62a2e25938ec");
        list.add(prefix + "uuid:094f9d18-c30e-4d3d-bc71-99785ebe1992");
        list.add(prefix + "uuid:5f823c49-a4ae-4a23-9c32-72406d1dc3f3");
        list.add(prefix + "uuid:5534caef-2e0f-4770-8ff9-07056655fae9");
        list.add(prefix + "uuid:4d19b637-ed2a-4842-acf7-3872bdd499b6");
        list.add(prefix + "uuid:6abd8659-d4e8-49cd-a689-9aea857bcfb0");
        list.add(prefix + "uuid:c1348c0d-3ef8-49cb-909c-3b81e597f2cb");
        list.add(prefix + "uuid:8e69e4ef-435c-4070-bddc-3c08a2c3b928");
        list.add(prefix + "uuid:bef41da4-cd71-493d-9228-c6e340d38cd5");
        list.add(prefix + "uuid:245db771-4dd6-4645-b155-c7b9c85ce79b");
        list.add(prefix + "uuid:f4ef17cf-5452-4c50-8ca8-bae9430e6fa0");
        list.add(prefix + "uuid:7fa80945-cf26-49d8-af22-4724c64a2c17");
        list.add(prefix + "uuid:41c839ea-fcbd-4fe3-8313-59cbc4571c7a");
        list.add(prefix + "uuid:3b4630da-9af1-411a-b5f6-55e71ad0ec60");
        list.add(prefix + "uuid:41e88ffa-b49f-4530-8697-a60b40c4172d");
        list.add(prefix + "uuid:26320e21-e89e-4f8f-a0f1-81a7913fb409");
        list.add(prefix + "uuid:45b66e27-cc17-4e00-a888-a25ba17ca221");
        list.add(prefix + "uuid:c3a47db3-6602-42df-95c4-f39cc9486a40");
        list.add(prefix + "uuid:c611a331-cc1f-4bdf-bd7b-ab3e2a56fd3f");
        list.add(prefix + "uuid:1486d213-cb32-4fce-8ce2-a85db5766f76");
        list.add(prefix + "uuid:4b71f019-8260-4cda-9d03-59744253c7b8");
        list.add(prefix + "uuid:5a6374d3-1df2-4188-81ce-12ccb10ed84b");
        list.add(prefix + "uuid:f9698a39-150e-4b5b-a146-c2cad897ee93");
        list.add(prefix + "uuid:958212b4-06c9-4e5f-9673-1f420d1b8994");
        list.add(prefix + "uuid:9b002ffb-6298-4c2d-992b-db15d33bd34e");
        list.add(prefix + "uuid:f3142703-091d-4236-95d5-5e48209ba961");
        list.add(prefix + "uuid:132d3021-30b2-4603-a882-451fa184f360");
        list.add(prefix + "uuid:aa0ed102-5c73-4931-8d86-54a54ffd456a");
        list.add(prefix + "uuid:e3ffb8f6-d9e4-48a5-8fa4-9ba478e1de12");
        list.add(prefix + "uuid:6f4ce74f-9dcc-4fe4-93ce-7a1411de70e3");
        list.add(prefix + "uuid:5fd793ff-2937-4c7c-816a-5c4f4a7a1704");
        list.add(prefix + "uuid:69662169-5382-486a-a393-46a2d347bf3e");
        list.add(prefix + "uuid:df21ced7-c6b6-4033-871e-044091121a92");
        list.add(prefix + "uuid:0dcb21f5-f250-47aa-a2df-7a347a1d5780");
        list.add(prefix + "uuid:06a02518-47ce-4582-8877-e07b1d473da4");
        list.add(prefix + "uuid:38d46d9d-0ca7-4e7d-a7af-5ca437519ede");
        list.add(prefix + "uuid:f59a3dd9-fa45-4d39-9707-42bfe66e9a40");
        list.add(prefix + "uuid:ff0832a6-e77b-4fd7-9463-ca4a2dbe2cbc");
        list.add(prefix + "uuid:10800748-fe19-438e-ad33-3479ae67ae1b");
        list.add(prefix + "uuid:b9ca1f15-e875-4aee-a8cd-00d3cade2ecd");
        list.add(prefix + "uuid:5935d3dd-a5d5-4548-b4ee-e89c2e3cefb6");
        list.add(prefix + "uuid:4fbbd57c-a7b2-4079-b7c0-98da60e44ca7");
        list.add(prefix + "uuid:2faaa904-5d07-469a-8644-d618000d2326");
        list.add(prefix + "uuid:3161a4c5-d5cf-4972-a88f-0d116eaf09ef");
        list.add(prefix + "uuid:aaa9c99b-637a-4124-a213-6be58bfb48fd");
        list.add(prefix + "uuid:16084f7b-7bac-4800-a2fd-b92d394314b3");
        list.add(prefix + "uuid:089720e8-ac5a-45b7-8c52-90272195450b");
        list.add(prefix + "uuid:5e83f514-045f-4a17-861e-ff51ad268111");
        list.add(prefix + "uuid:25285b46-2779-432a-96e1-3cf77d11656a");
        list.add(prefix + "uuid:62edca05-18e2-49d7-a19c-13d6260f69b9");
        list.add(prefix + "uuid:78987900-0f67-47bb-ab13-ead99dbe6265");
        list.add(prefix + "uuid:bb7d5cc4-455c-428b-a537-1a82694661f5");
        list.add(prefix + "uuid:e4803ae8-1403-4c7d-8418-cf3bd47c7a51");
        list.add(prefix + "uuid:3c1f121c-9ec0-4e21-aca7-7783fc6ca794");
        list.add(prefix + "uuid:cf470f80-057c-4544-ac83-a491652d9ce7");
        list.add(prefix + "uuid:9e26e005-3538-4156-8e85-4a39bba90303");
        list.add(prefix + "uuid:23a7f9b2-2841-478a-86ce-0d09cbc99242");
        list.add(prefix + "uuid:d3cc610c-a0e8-4fbf-a71a-3ef839182ce8");
        list.add(prefix + "uuid:abf90525-b69f-46d5-9ba0-b8b4b8cce620");
        list.add(prefix + "uuid:9daa50f7-6a4c-4106-ad9d-7dd344012b03");
        list.add(prefix + "uuid:25277326-3c53-4c27-9e75-0a2c6b032c8e");
        list.add(prefix + "uuid:b4cda17a-bad9-4cc8-bfb4-2d78ef99539b");
        list.add(prefix + "uuid:d933eefc-be20-43d3-805b-f17ecf1c361f");
        list.add(prefix + "uuid:113fbba8-77f1-4fa5-8e06-0f86a51be0b5");
        list.add(prefix + "uuid:d97f1f04-90c1-4ad8-b98c-29b8f5fc4cbc");
        list.add(prefix + "uuid:4d79af51-16c1-449a-9496-6d50bc37aaf8");
        list.add(prefix + "uuid:bfcd1c1e-861f-49bc-bd3e-0dd19c546ee4");
        list.add(prefix + "uuid:1e315f92-adc9-4a9f-a08e-8230d066471d");
        list.add(prefix + "uuid:bc783bff-ae41-44d0-a337-e0967002c2d3");
        list.add(prefix + "uuid:24716d63-4a82-4afd-b002-63a653534d16");
        list.add(prefix + "uuid:52d978e4-6255-4f18-8db5-ab50368ba033");
        list.add(prefix + "uuid:f75dce81-52c7-445e-94ce-b69dcc84441f");
        list.add(prefix + "uuid:f23bf01c-17a3-4ab4-b856-beb08f74a0e9");
        list.add(prefix + "uuid:20445316-2f23-4f5a-a24f-05ce2729c9b4");
        list.add(prefix + "uuid:77c99b7c-0b6c-45b4-8e61-5e77464f331a");
        list.add(prefix + "uuid:5aea11f5-04f8-44d5-8995-22b1ce29aa0c");
        list.add(prefix + "uuid:0c907c0b-b39a-46cb-b296-9b8d3cb73da7");
        list.add(prefix + "uuid:2f81684e-499a-41da-838e-dc084c0168c9");
        list.add(prefix + "uuid:738e9041-9dc9-4ad1-8990-64c8ead4a546");
        list.add(prefix + "uuid:10709cae-e9cf-4c84-b512-5074b98b513e");
        list.add(prefix + "uuid:7c044445-ee10-47c8-8dea-ab096ab63cbe");
        list.add(prefix + "uuid:dcea5a20-5286-45ca-9db4-9155d98c31ba");
        list.add(prefix + "uuid:0b417782-98ef-427a-add8-3593a9933a6c");
        list.add(prefix + "uuid:3ac0401d-991d-4007-a37f-847cb8b997dd");
        list.add(prefix + "uuid:2e4c1e82-41d7-4294-82fd-efdcba3f18d8");
        list.add(prefix + "uuid:a71ca4a4-df2a-42ef-aa0b-72283a7af356");
        list.add(prefix + "uuid:ff1be46e-179d-4471-95fb-206387995ea0");
        list.add(prefix + "uuid:43fdaa1a-2291-4ba9-9990-bc35e747d341");
        list.add(prefix + "uuid:5957d128-8e88-45b0-b86e-47e7f6b4df9c");
        list.add(prefix + "uuid:eadd5de2-2495-4639-a4d7-ab18150b45da");
        list.add(prefix + "uuid:a28b2fb2-8659-41c6-8239-527c9af39831");
        list.add(prefix + "uuid:430625a8-aa92-40f4-bafe-92a977d7a22b");
        list.add(prefix + "uuid:b88a0ab6-c6f6-449b-b80c-14fd186b873f");
        list.add(prefix + "uuid:99ceecf5-1cd2-4dda-85ef-9b5fb43a7fc6");
        list.add(prefix + "uuid:d4acfd72-dfff-4aa5-8dcd-40f3b9633654");
        list.add(prefix + "uuid:fc781b5b-f6e5-4247-855e-07fd2700d365");
        list.add(prefix + "uuid:ecccfee8-5daa-43a8-b50b-0230e5ceebb9");
        list.add(prefix + "uuid:fa759c95-8afa-4729-8136-c4679aed7776");
        list.add(prefix + "uuid:3f2bc9a3-41a0-4e6f-b9ed-1862fc0ce7bc");
        list.add(prefix + "uuid:94db7b43-d1c8-4b95-a6c1-e711b49514d0");
        list.add(prefix + "uuid:0d4d60b4-9fa2-49e6-b108-b0f9962d58e5");
        list.add(prefix + "uuid:0534725a-b65c-4082-b948-5e8c9e55b50f");
        list.add(prefix + "uuid:0aba93fc-6ae7-46a1-a415-b8df42be5841");
        list.add(prefix + "uuid:2c568467-15eb-439b-8cb2-06ce8c36b65f");
        list.add(prefix + "uuid:eadd3c4b-92fa-48a0-a461-ec0a3c7a152d");
        list.add(prefix + "uuid:9facd3ea-3634-42fb-8fe0-84b519a36c6b");
        list.add(prefix + "uuid:bd679bf5-af53-455b-81d1-3764f7d30c07");
        list.add(prefix + "uuid:e4ad9e7d-b970-4377-a52f-445f983918d1");
        list.add(prefix + "uuid:45b72931-7c85-4de9-87dd-d78d7b147a39");
        list.add(prefix + "uuid:89fce47b-78ab-4045-9863-c137de69b7b9");
        list.add(prefix + "uuid:7031a03c-b1b2-4f18-ad9f-05c6ca7d503a");
        list.add(prefix + "uuid:6e8cc8c2-5e82-4508-a01d-4e82b2bdb937");
        list.add(prefix + "uuid:999f05b7-6123-4c78-a0e2-9207a34be1b8");
        list.add(prefix + "uuid:4e5af465-96bd-4a0f-8ab2-c3f5bd8e2560");
        list.add(prefix + "uuid:baab178e-15a2-4b66-80ca-25425ee7eab7");
        list.add(prefix + "uuid:bbf189bc-c40c-4b1e-b8d0-a60dc1920112");
        list.add(prefix + "uuid:537b37dd-ce76-4a73-a0ae-f79eb2a33571");
        list.add(prefix + "uuid:774e688f-ad41-4997-ac11-99a7bc13b460");
        list.add(prefix + "uuid:7b65e01d-6c05-47ed-88c6-f838572603db");
        list.add(prefix + "uuid:280c36d2-97ba-425b-868b-beb184c1b92a");
        list.add(prefix + "uuid:46f90be3-cd0b-4163-97fa-9c5223c0cbc6");
        list.add(prefix + "uuid:cf10674a-5844-4376-832d-2ac98e8533f8");
        list.add(prefix + "uuid:fb83b6c7-5ddc-48e9-9852-171471e3abf4");
        list.add(prefix + "uuid:7d8bbf3b-1ae7-40ae-9e36-6ba355d6e4ac");
        list.add(prefix + "uuid:1737570d-c367-4092-a150-96485c905cc8");
        list.add(prefix + "uuid:20c92659-01d9-4d24-81c3-4eb64f801943");
        list.add(prefix + "uuid:aea47954-b6fd-4f51-86e5-9416a7b467a6");
        list.add(prefix + "uuid:a8810560-d710-4463-a1dd-43f4ede62bd5");
        list.add(prefix + "uuid:c1e62b18-13a3-443b-8935-feaed96279e9");
        list.add(prefix + "uuid:651c481e-58fe-43f6-9154-7b8e98e70740");
        list.add(prefix + "uuid:093a6f74-19cf-41d6-a04d-d2a950f8db83");
        list.add(prefix + "uuid:2f98c62c-529f-4c57-8cbe-6f8bf9bef042");
        list.add(prefix + "uuid:409a3773-8322-4977-95ea-e1e722502ec5");
        list.add(prefix + "uuid:bf265fdd-10a8-449e-83c7-2684580f453e");
        list.add(prefix + "uuid:884fa442-b5ac-4388-928f-8b23cee2c604");
        list.add(prefix + "uuid:f951b4a5-0365-47c6-ba76-4052a681a89c");
        list.add(prefix + "uuid:3f80c1c9-5707-46ea-b41f-ab6a6c9bb2bd");
        list.add(prefix + "uuid:f7139fd6-a2af-4bb2-a663-3bb388c9cfa6");
        list.add(prefix + "uuid:4af8c53f-8721-4e4f-9c9d-0e21e2ee5c27");
        list.add(prefix + "uuid:75703bc8-d566-4653-b599-1313e4de23b8");
        list.add(prefix + "uuid:7a6f48a8-4abe-4c14-9c8c-1ac66ad1ae50");
        list.add(prefix + "uuid:4e53b26f-bf8a-406b-b887-5a4ed7f08759");
        list.add(prefix + "uuid:625077f6-2bcb-4123-be6e-dd58554b7309");
        list.add(prefix + "uuid:d7298d95-1011-4356-8d5e-495fc7b66f13");
        list.add(prefix + "uuid:bb22d560-87d2-4ed0-b80c-527b08415bbe");
        list.add(prefix + "uuid:60bda35d-35ff-4c22-972a-874a58de98bf");
        list.add(prefix + "uuid:5836bcbe-8234-4ba4-a5a7-f3580a87ea80");
        list.add(prefix + "uuid:df360ba9-4d79-4ab7-ab47-512557216451");
        list.add(prefix + "uuid:c9a996ab-81e0-47a5-9f6e-27c7943e9774");
        list.add(prefix + "uuid:0f81f005-f36a-44ec-b461-822fdb3fb15e");
        list.add(prefix + "uuid:a121bbae-14f5-4faf-b2b4-9a493e92d708");
        list.add(prefix + "uuid:261ae003-2ed1-4552-bbdb-421878b6deff");
        list.add(prefix + "uuid:24a8fcb0-b919-42e0-a366-23b6414c9a8a");
        list.add(prefix + "uuid:29e2103a-4ac9-4d32-84fd-ce67fa5fa51c");
        list.add(prefix + "uuid:b7e121e3-18a4-4389-9c6c-c0d845a596c7");
        return list;
    }
}
