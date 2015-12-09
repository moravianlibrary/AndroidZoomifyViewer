package cz.mzk.tiledimageview.demonstration.kramerius;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Martin Řehánek
 */
public class KrameriusExamplesFactory {

    private static final String TAG = KrameriusExamplesFactory.class.getSimpleName();

    public static ArrayList<MonographExample> getTestTopLevelUrls() {
        String krameriusMzk = "kramerius.mzk.cz";
        String krameriusNdkMzk = "krameriusndktest.mzk.cz";
        String dockerMzk = "docker.mzk.cz";
        String krameriusTestMzk = "krameriustest.mzk.cz";
        String krameriusLibCas = "kramerius.lib.cas.cz";
        String krameriusFsvCuni = "kramerius.fsv.cuni.cz";
        String krameriusZcm = "kramerius.zcm.cz";

        ArrayList<MonographExample> result = new ArrayList<MonographExample>();

        result.add(new MonographExample(
                "http://kramerius.mzk.cz/search/handle/uuid:44e2293b-6409-43dc-92af-f661c0369533", "Varujeme!",
                "white rectangle problem", krameriusMzk));
        result.add(new MonographExample(
                "http://krameriusndktest.mzk.cz/search/handle/uuid:0d8192f0-320f-11e2-824c-005056827e51",
                "Powinnosti manželů, rodičů, hospodářů a poddaných křesťanských", "tiles problem", krameriusNdkMzk));
        result.add(new MonographExample(
                "http://kramerius.mzk.cz/search/handle/uuid:d088d506-97b9-45f1-89e6-b965f90a89e8",
                "General-Karte des Fürst zu Fürstenbergschen Fideikommissbesitzstandes", "working tiles", krameriusMzk));
        result.add(new MonographExample(
                "http://kramerius.mzk.cz/search/handle/uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22",
                "Když slunéčko svítí", "working tiles", krameriusMzk));
        result.add(new MonographExample(
                "http://kramerius.mzk.cz/search/handle/uuid:4873e8c7-5967-4003-8544-96f64ca55da7",
                "Symbiotické zemědělství", "no tiles, no jpegs, just single pdf", krameriusMzk));
        result.add(new MonographExample(
                "http://kramerius.mzk.cz/search/handle/uuid:a980edb0-a1e3-11dd-b501-000d606f5dc6",
                "Mladá fronta (1949)", "no tiles, just jpegs, no rights", krameriusMzk));
        result.add(new MonographExample(
                "http://kramerius.mzk.cz/search/handle/uuid:f5a09c95-2fd8-11e0-83a8-0050569d679d", "Máj",
                "no tiles, just jpegs, working (book)", krameriusMzk));
        result.add(new MonographExample(
                "http://kramerius.mzk.cz/search/handle/uuid:4689b175-f1e1-11e1-a3c6-0050569d679d", "Divadelní šepty",
                "no tiles, just jpegs, working (newspaper)", krameriusMzk));
        result.add(new MonographExample(
                "http://krameriustest.mzk.cz/search/handle/uuid:8069c22a-0ab2-41c3-b7a5-5dcca16ce089",
                "Chrudimský kancionál", "no tiles", krameriusTestMzk));
        result.add(new MonographExample(
                "http://kramerius.mzk.cz/search/handle/uuid:6430d7ae-0ea5-4587-9aab-9d7d9c42a791",
                "Kde slunce výjde zítra", "tiles, no access rights", krameriusMzk));
        result.add(new MonographExample(
                "http://kramerius.mzk.cz/search/handle/uuid:5e1e9cd8-eecd-4627-9a1a-09c53caaf9a8",
                "Stát československý", "", krameriusMzk));
        result.add(new MonographExample(
                "http://kramerius.mzk.cz/search/handle/uuid:36e7c070-4bd3-4bc4-b991-3b66fe16f936",
                "[Thajský rukopis na palmových listech]", "", krameriusMzk));
        result.add(new MonographExample(
                "http://kramerius.mzk.cz/search/handle/uuid:b73aff3f-5420-41d5-8adf-e1086492eb80",
                "Weypis oné žalostné Cesty Gežjsse, Marye, a Jozeffa, do Egipta z Nazaretu : kterak welké Zármutky, Strach, Hlad, Žjžeň, ty tři Swaté Osoby zakusili, a gak welicý Zázrakowé na té Cestě se dáli : Wytažený z Žiwota Krysta Pána, a Panny Marye, a nynj w Pjseň vwedeno",
                "", krameriusMzk));
        result.add(new MonographExample(
                "http://kramerius.mzk.cz/search/handle/uuid:db53e7df-82dc-11e0-bc9f-0050569d679d",
                "Grosser Erdbeschreibung. Blatt N. 698-802.", "", krameriusMzk));
        result.add(new MonographExample(
                "http://kramerius.mzk.cz/search/handle/uuid:a69b1ae8-82e1-11e0-bc9f-0050569d679d",
                "Atlas minor Sive totius orbis terrarum contracta delinea[ta] ex conatibus", "", krameriusMzk));
        result.add(new MonographExample(
                "http://kramerius.mzk.cz/search/handle/uuid:1a232219-82e0-11e0-bc9f-0050569d679d",
                "Atlas von den an Böhmen und Mähren gränzenden Fürstenthümern Schlesiens mit der Grafschaft Glatz", "",
                krameriusMzk));
        result.add(new MonographExample(
                "http://kramerius.mzk.cz/search/handle/uuid:392a9006-1c98-4903-97f4-14212d097c99",
                "[Administrativ-Karte des Erzherzogthumes Oesterreich ob der Enns]. Blatt 16, Umgebungen von Kirchdorf und Windischgarsten",
                "", krameriusMzk));
        result.add(new MonographExample(
                "http://kramerius.mzk.cz/search/handle/uuid:51ec134e-c700-4125-bfb6-422ebf2cc664",
                "Abhandlung Dreyer so nothwendig- als nützlichen INSTRUMENTEN", "", krameriusMzk));
        result.add(new MonographExample(
                "http://kramerius.mzk.cz/search/handle/uuid:e256d5f7-4d86-4324-90f5-4f398b9427c4",
                "Anti-Alkoran : To gest: Mocnj a nepřemoženj důvodové toho, že Alkorán Turecký z ďábla possel, a to půwodem Aryánů s wědomým proti Duchu Swatému rauhánjm",
                "", krameriusMzk));
        // https
        result.add(new MonographExample(
                "https://docker.mzk.cz/search/handle/uuid:8ffd7a5b-82da-11e0-bc9f-0050569d679d",
                "Atlas de l'Amerique Consistant en 46. Cartes hollandoises et angloises", "HTTPS - working", dockerMzk));
        result.add(new MonographExample("http://docker.mzk.cz/search/handle/uuid:8ffd7a5b-82da-11e0-bc9f-0050569d679d",
                "Atlas de l'Amerique Consistant en 46. Cartes hollandoises et angloises", "HTTP -> HTTPS redirection",
                dockerMzk));
        result.add(new MonographExample(
                "https://kramerius.lib.cas.cz/search/handle/uuid:4240f893-7853-4088-9a07-a6cdb81f3631",
                "Biblia latina", "HTTPS - no tiles, just jpegs", krameriusLibCas));
        result.add(new MonographExample(
                "https://kramerius.fsv.cuni.cz/search/handle/uuid:9711c868-2fd1-11e0-a23a-0050569d679d",
                "Jan Amos Komenský, vínek z pomněnek", "HTTPS - invalid date", krameriusFsvCuni));
        result.add(new MonographExample(
                "https://krameriusndktest.mzk.cz/search/handle/uuid:70680130-01ff-11e4-9789-005056827e52",
                "Veselé čtení z desíti let", "HTTPS - invalid certificate (common name wrong)", krameriusNdkMzk));
        result.add(new MonographExample(
                "https://kramerius.zcm.cz/search/handle/uuid:981c2b8d-2042-11e3-88f5-001b63bd97ba", "Stavba",
                "HTTPS - invalid certificate (common name wrong)", krameriusZcm));

        // result.add(new MonographExample(
        // "http://krameriusndktest.mzk.cz/search/handle/uuid:efd23340-c9da-11e2-b6da-005056827e52",
        // "Písně pro školní mládež",
        // krameriusNdkMzk));

        // divne se zobrazuji:
        // zobrazuje se divne, prvni polovina jakoby roztazene
        result.add(new MonographExample(
                "http://kramerius.mzk.cz/search/handle/uuid:831e1f40-d297-42af-a06f-c1e04863f043",
                "Allerneuste Geheimniße der Freymäurer", "erroneous tiles or bug in viewer", krameriusMzk));
        // vypada to, jako by to slo az od 30 strany
        // do konce se to i na webu zobrazuje divne - dve stranky pod sebou v
        // prohlizecce, kdyz to rozkliknu pres strom
        result.add(new MonographExample(
                "http://kramerius.mzk.cz/search/handle/uuid:0b4d383c-3616-4116-ac07-d7f6bd98e7a6",
                "Allerneuste Geheimniße der Freymäurer", "erroneous tiles or bug in viewer", krameriusMzk));

        // obrazky nejsou v image serveru:
        // result.add(new MonographExample(
        // "http://kramerius.mzk.cz/search/handle/uuid:95efa662-9110-11e0-af9b-0050569d679d",
        // "NO_TILES: Atlantis", krameriusMzk));
        // result.add(new MonographExample(
        // "http://kramerius.mzk.cz/search/handle/uuid:8d818753-62e3-11e1-8115-0050569d679d",
        // "NO_TILES: 700 letá slawnost poswátného poutního místa Marie-Celly, od 1. ledna až do 31. prosince, léta Páně 1857, kteráž úplnými jubilejními odpustky Swatým Otcem, papežem Piem IX., nadána jest pro wssecky poutníky Marie Cellské",
        // krameriusMzk));
        //
        // // nezacina na prvni strance (v jsonu budou typu "predni vazba"
        // apod.)
        // result.add(new MonographExample(
        // "http://kramerius.mzk.cz/search/handle/uuid:220297ba-17c3-469d-a55a-56c8808e616f",
        // "900 let rajhradského kláštera (1048-1948)", krameriusMzk));

        // bez prav ke cteni
        // result.add(new MonographExample(
        // "http://krameriusndktest.mzk.cz/search/handle/uuid:bc6c7800-ebb5-11e3-b72e-005056827e52",
        // "NO_RIGHTS: Vodní rod", krameriusNdkMzk));
        return result;
    }

    public static List<String> getTestPages(String topLevelPid) {
        if ("uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22".equals(topLevelPid)) {
            List<String> result = new ArrayList<String>();
            result.add("uuid:afdd8ea1-ad6f-474c-9611-152cfd3a14b3");
            result.add("uuid:3e7e7e61-a835-4b50-8162-052437fdd718");
            result.add("uuid:948250f6-6cb8-4fff-915d-67797655024d");
            result.add("uuid:ecb7b0e4-59fb-4a50-9cf3-387a386b055e");
            result.add("uuid:e24312f0-f83a-4c0c-87be-6ae1cccb1208");
            result.add("uuid:5b93cdc8-6f6e-4d54-ace0-f084a99e610f");
            result.add("uuid:df34f872-c9db-4034-884d-c74448ba6e77");
            result.add("uuid:a56d5e12-e7ec-43ca-acfa-1dbe12e47de5");
            result.add("uuid:e8f58ced-7631-45de-b77e-feae41e05af4");
            result.add("uuid:40d779fa-b2d9-4a85-857b-ee4febc477f1");
            result.add("uuid:891ee7f6-42dc-42ae-8b97-475e30dca18d");
            result.add("uuid:882a8f4f-1dde-4597-802b-77e16c0f68cf");
            result.add("uuid:a48435d8-3135-4ad8-9a0c-c0bcb3ff171f");
            result.add("uuid:038f46ec-e199-4823-87ca-0f25d14163d0");
            result.add("uuid:676f07f1-0969-4022-b336-7227e0800341");
            result.add("uuid:5de8741e-3f83-49f8-b7a6-274e1f49603b");
            result.add("uuid:ce36d3a4-fd97-4439-9bff-8524a6010be7");
            result.add("uuid:57a5ef4f-1d05-4211-95cc-80ebf2c5099d");
            result.add("uuid:841c1942-b23b-4c72-a099-8336dac6e162");
            result.add("uuid:41d99e5d-d525-416c-ba99-554da9018bd6");
            result.add("uuid:133204f8-0c25-41a9-8537-7e3ee85e7389");
            result.add("uuid:c99bb091-fa22-4399-96cd-739a9e66bbc6");
            result.add("uuid:1a5259e1-b93e-4f9a-a9ff-60cf433dcc3b");
            result.add("uuid:e9898ac4-bf80-4e2a-a60e-3ae6cfd696f9");
            result.add("uuid:2419696d-050f-45cd-b8fe-19c5ffb4fb4c");
            result.add("uuid:a00d87f8-9ac8-4e66-b67c-b89c943fbb77");
            result.add("uuid:20dd871a-624e-4432-9aaf-7bc7d7098a12");
            result.add("uuid:a31e2fab-edd2-4a63-bbb9-2d9ddf72d74c");
            result.add("uuid:d5c1c435-8adc-4028-a997-9c7eb131fbc4");
            result.add("uuid:40b487d1-c252-431a-be6b-d9c889c393cd");
            result.add("uuid:ecc519de-ae95-43ee-9d3a-35585d66b646");
            result.add("uuid:9ec55500-e566-4048-91d9-0d1c3f268739");
            result.add("uuid:1710071f-a507-4a55-8e4f-a1c8db66b2d0");
            result.add("uuid:27f386be-7fa6-45d4-b171-3cedac2e15c1");
            result.add("uuid:de92106b-bd79-4ce2-9043-df3fd62c875f");
            result.add("uuid:8e121d4a-5835-4d76-995a-a6235779fd1f");
            result.add("uuid:db907001-c873-4ca9-909d-4d34279035fb");
            result.add("uuid:24330953-cccc-4c06-b4f9-859dd01fa2e7");
            result.add("uuid:3aca6ad6-17c9-401d-a9ab-90fdbb219e5f");
            result.add("uuid:392680a9-dbe1-413d-9459-1dc516ffad0b");
            result.add("uuid:693c2b07-637e-46ae-b65e-0246561009ee");
            result.add("uuid:8f536d9d-824a-422c-8d36-1c00cf3517ef");
            result.add("uuid:ea67a22c-6d57-4933-b34b-20278bb6baff");
            result.add("uuid:2eee14f7-6f1b-49cc-8de3-77439e75ba13");
            result.add("uuid:d8a1087c-578b-43d8-b850-7fb5e178c1a8");
            result.add("uuid:8f27ce46-6d26-42ad-bb2a-c626377442e2");
            result.add("uuid:ab50142c-db04-4e09-acf7-b60b59c70176");
            result.add("uuid:0ca53f78-bd84-492b-aa4a-8e8734b7293d");
            result.add("uuid:1396fb76-fd5c-443b-a894-fe0a2d0149dd");
            result.add("uuid:5b089ff9-bee7-4d34-9a9f-ed7e4f24a007");
            result.add("uuid:90860359-4778-40ee-8da3-4bef59bc42d9");
            result.add("uuid:43a1a9e3-bf8a-458b-bc38-cd5f46f0cf73");
            result.add("uuid:7d42a902-4a7c-4328-9317-484c3ad17e18");
            result.add("uuid:1d5b2dc1-1e76-4d2f-87d7-2ff9943a45a3");
            result.add("uuid:e41bdd3d-62d4-4916-9901-a7be90613fcd");
            result.add("uuid:321d5be2-320e-4180-b10e-86446e37622e");
            result.add("uuid:e4e809d0-6e75-4a9c-bcf8-234cec80e773");
            result.add("uuid:9a3081c5-d47b-470c-890f-8d80d1e2e384");
            result.add("uuid:0789cc9d-6fbd-4126-8aa5-46f6ed8205f8");
            result.add("uuid:93ddf659-eee4-4ba0-a61f-18c537f4480e");
            return result;
        } else if ("uuid:206aac01-e915-4806-a828-324d3d8ee525".equals(topLevelPid)) {
            List<String> result = new ArrayList<String>();
            result.add("uuid:009c922e-41e3-46eb-8050-34886a02f577");
            result.add("uuid:902b4314-98e7-4e7f-8947-3f6f28c4e490");
            result.add("uuid:2c6c7c6c-d42d-4152-9ca8-131eef63a22a");
            result.add("uuid:cfa6c4fb-397b-42f4-8b7d-8ce08c01af86");
            result.add("uuid:4fcbce18-c05f-41f5-8560-b374bbf3e240");
            result.add("uuid:8e088ee2-035e-499a-b65f-157d6c701757");
            result.add("uuid:b3d9f83c-dbc2-4832-878e-cf208dcf31ae");
            result.add("uuid:cb1e742b-58ff-4378-8c42-d6b857bd2857");
            result.add("uuid:8b43bc4a-746d-49a3-90cc-0bc3e01869d4");
            result.add("uuid:a6e60eb4-1d44-4cb2-baee-e6ec98dd3211");
            result.add("uuid:e2517266-da18-4310-a533-2742f330fe03");
            result.add("uuid:3f75d9ef-ff6f-4622-ad23-f084817d3ed2");
            result.add("uuid:3f46f15e-9c1f-466b-8aab-33bd614516a0");
            result.add("uuid:ac7d6d0e-793a-431c-abf4-1a8746d3cf9a");
            result.add("uuid:b49bc717-522d-4764-a42e-403fa3333138");
            result.add("uuid:1366f8fa-2dcc-4099-b32f-cf9f939df0d6");
            result.add("uuid:2c0482d1-70f9-4041-bc3c-09d3b5f68e02");
            result.add("uuid:bf650189-ec96-4de5-8e7f-c75a0508d84d");
            result.add("uuid:d54ea5a0-0c3a-4fc6-a78a-47901586490b");
            result.add("uuid:bf5d6aba-c878-4a8d-b52e-f99350e35514");
            result.add("uuid:6128edcc-55b4-49dc-9c12-7967a277255e");
            result.add("uuid:22afed76-72ac-4584-a4c0-b5da46c3f09c");
            result.add("uuid:839974aa-6f39-44d7-859d-823362a85b69");
            result.add("uuid:d819287f-40d8-4be4-b1c3-f036d50b3de4");
            result.add("uuid:2afe837d-7bc0-4f86-8d0e-fd3817336430");
            result.add("uuid:210720d4-3ee4-4de4-a588-33087e4b5625");
            result.add("uuid:03b50d46-32c7-476b-bd09-34f33fd881e0");
            result.add("uuid:ab7e5981-b79c-4942-b786-b3dfb5adfe3d");
            result.add("uuid:c7d9f320-b374-4635-87e2-7eee2c636795");
            result.add("uuid:f92b00fc-4c89-41ed-9894-df8add88aaa7");
            result.add("uuid:5aa74fcf-52a1-4b79-a728-c69276ffb995");
            result.add("uuid:4c7186c6-cfd3-4dc1-8bc7-3db7e9f7af29");
            result.add("uuid:6a537905-f4da-48c5-a0b3-83d633624e2d");
            result.add("uuid:196abbe1-e521-4f50-85dc-69602a6fa0b1");
            result.add("uuid:3b586a03-2fb8-4df5-8c35-0d22f45789da");
            result.add("uuid:b71f3151-6b58-4f2a-afbe-ecfc3dc6f2c9");
            result.add("uuid:f5a326cb-8b35-480c-b62d-5077c1aa491f");
            result.add("uuid:150752e8-e71e-45b9-85fc-261826f6ef79");
            result.add("uuid:15931f97-69a9-46f8-8886-a9502d79c173");
            result.add("uuid:e4cd9a1e-efcc-4b25-8925-90d6e2590da7");
            result.add("uuid:2afa8766-208b-46f3-bd7f-0338bec0c5a6");
            result.add("uuid:d27b21c2-ee0a-47cd-994b-209455282839");
            result.add("uuid:3373a09b-9f7f-4cdf-8d2b-01622dc16177");
            result.add("uuid:abd23c70-ded9-46f8-b40f-710b74f223c5");
            result.add("uuid:372c9cb6-ebdd-4250-9d99-6ffb5613ebab");
            result.add("uuid:be3ce7a2-f147-4cf7-aa46-508f8ec836b2");
            result.add("uuid:9e568f3d-16bc-4fa5-8a18-612ca2e74ed4");
            result.add("uuid:821b6258-43d0-42c8-ad27-5b83bb6667bc");
            result.add("uuid:4a35557d-43c8-4edd-8d45-b46a6b638cd2");
            result.add("uuid:ae90a278-1e5b-468a-adf3-aecd5ba640c1");
            result.add("uuid:a90051f1-6186-497c-9d5a-3557d7c21c0b");
            result.add("uuid:fa8f50d0-e4ca-4ea8-96a3-1574dd4ea2b2");
            result.add("uuid:0af2c784-d774-4411-9cc3-848de4d6af2a");
            result.add("uuid:2db0694f-8f70-487d-9de0-3046f005cc3c");
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    public static List<String> getTestPages() {
        List<String> result = new ArrayList<String>();
        // vymyslene
        result.add("uuid:5b2637c0-0c18-11e4-8c14-5ef3fc9bb22f");
        // nejsou prava
        result.add("uuid:bc6c7800-ebb5-11e3-b72e-005056827e52");
        result.add("uuid:5a0c2010-0c18-11e4-8c14-5ef3fc9bb22f");
        result.add("uuid:5a2637c0-0c18-11e4-8c14-5ef3fc9bb22f");
        return result;
    }

    public static class MonographExample {
        private static final int MAX_TITLE_LENGTH = 30;
        private static final String TITLE_SUFFIX = " ... ";

        private final String url;
        private final String title;
        private final String note;
        private final String source;

        public MonographExample(String url, String title, String note, String source) {
            this.url = url;
            if (title.length() > MAX_TITLE_LENGTH) {
                int titleSubstringLength = Math.min(title.length(), MAX_TITLE_LENGTH - TITLE_SUFFIX.length());
                this.title = title.substring(0, titleSubstringLength) + TITLE_SUFFIX;
            } else {
                this.title = title;
            }
            this.note = note;
            this.source = source;
        }

        public String getUrl() {
            return url;
        }

        public String getTitle() {
            return title;
        }

        public String getSource() {
            return source;
        }

        public CharSequence getNote() {
            return note;
        }
    }

}
