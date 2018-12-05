package com.example.administrator.saytounlock;

import java.util.Random;

public class RandomKeyword {
    private String [] Tkeywords = {"House", "Elephant", "Intelligence", "Language", "Smart", "Plastic", "Concrete", "Alaska", "Tempest", "Segment"};
    private String [] Rkeywords = {"person","clothes","movie","activity","example","letter","minute","part","plan","vacation","round","advertise",
        "course","space","street","side","paper","newspaper","face","mind","volunteer","change","visit","start","watch","lovely","soft","empty",
        "light","preset","favorite","enjoy","understand","clean","please","interesting","famous","special","just","merchant","narrator","disappear",
        "organization","kindergarten","management","operation","mission","confidence","merit","mirror","ladder","igredient","index","arrange","protect",
        "behave","compete","argue","release","pretend","conclude","valuable","terrific","steady","spare","unique","unncessary","bound","underground",
        "sincerely","dinosaur","ruler","admiral","liberty","unification","trial","treatment","shelter","orphanage","item","firewood","range","horizon",
        "rumor","unite","associate","identify","mention","establish","entertain","frustrate","blond","ceerful","careless","dull","disgusting","eastern","civil",
        "apart","skin","blanket","chalk","document","license","fortune","resource","hunger","error","crime","jail","progress","request","electric",
        "trade","poison","wound","chase","absorb","attach","cooperate","defend","aware","obvious","precious","political","illegal","indoor","succeed",
        "solar","occupation","instruction","principle","tradition","revolution","authority","rank","property","advantage","affair","occasion",
        "protest","propose","approve","admit","conduct","reserve","reveal","extend","involve","definite","significant","vital","extreme","academic","guilty",
        "generally","ancestor","architect","enemy","throat","illness","drug","weed","humor","volcano","kingdom","attempt","award","broadcast","simply",
        "escape","career","conomy","construction","mood","excitement","complaint","invasion","container","announce","represent","devote","peaceful",
        "painful","sharp","excellent","responsible","public","giant","bend","float","concentrate","handsome","personal","historic","western","anyway",
        "forward","exactly","flour","neighborhood","cyberspace","situation","strength","recognize","celebrate","helpful","convenient","impossible",
        "medical","outdoor","daily","indeed","nowadays","gather","achieve","appreciate","brave","cheap","classical","familiar","unknown","uncomfortable",
        "intelligent","eager","disabled","homeless","harmful","lifelong","nearby","seldom","somewhere","childhood","education","emotion","attitude","courage",
        "condition","fever","conversation",};

    public RandomKeyword() {

    }

    public String getKeyword() {
        Random random = new Random();
//        return Tkeywords[random.nextInt(Tkeywords.length)];
        return Rkeywords[random.nextInt(Rkeywords.length)];
    }
}
