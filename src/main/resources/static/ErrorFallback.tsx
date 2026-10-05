const errorIcon1 =
  "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 96 96'%3E%3Ccircle cx='48' cy='48' r='44' fill='%23fde8e7'/%3E%3Cpath d='M48 24v28m0 14h.04' fill='none' stroke='%23c62828' stroke-linecap='round' stroke-width='8'/%3E%3C/svg%3E";

const errorIcon =
  "data:image/svg+xml,%3csvg%20width='96'%20height='96'%20viewBox='0%200%2096%2096'%20fill='none'%20xmlns='http://www.w3.org/2000/svg'%3e%3cpath%20opacity='0.5'%20d='M25%2017.7484C15.8841%2024.6898%2010%2035.6577%2010%2047.9999M48%209.99988C42.8651%209.99988%2037.9681%2011.0184%2033.5%2012.8643M78.9496%2015.9498L82.4852%2019.4853M85.3134%2022.3137L88.8489%2025.8492M82.4854%2022.3137L78.9499%2025.8492M85.3137%2019.4853L88.8492%2015.9498M88%2073L82%2079H89L83%2085M16%2074L12%2078H19L15%2082'%20stroke='%23FF435A'%20stroke-width='2'%20stroke-linecap='round'/%3e%3cpath%20opacity='0.3'%20fill-rule='evenodd'%20clip-rule='evenodd'%20d='M84%2050.9999C84%2069.2253%2069.2254%2083.9999%2051%2083.9999C32.7746%2083.9999%2018%2069.2253%2018%2050.9999C18%2032.7745%2032.7746%2017.9999%2051%2017.9999C69.2254%2017.9999%2084%2032.7745%2084%2050.9999ZM61.2071%2035.7929C61.5976%2036.1834%2061.5976%2036.8166%2061.2071%2037.2071L50.4142%2048L61.2071%2058.7929C61.5976%2059.1834%2061.5976%2059.8166%2061.2071%2060.2071C60.8166%2060.5976%2060.1834%2060.5976%2059.7929%2060.2071L49%2049.4142L38.2071%2060.2071C37.8166%2060.5976%2037.1834%2060.5976%2036.7929%2060.2071C36.4024%2059.8166%2036.4024%2059.1834%2036.7929%2058.7929L47.5858%2048L36.7929%2037.2071C36.4024%2036.8166%2036.4024%2036.1834%2036.7929%2035.7929C37.1834%2035.4024%2037.8166%2035.4024%2038.2071%2035.7929L49%2046.5858L59.7929%2035.7929C60.1834%2035.4024%2060.8166%2035.4024%2061.2071%2035.7929Z'%20fill='%23FF435A'/%3e%3cpath%20d='M59.5%2035.5L36.5%2058.5M59.5%2058.5L36.5%2035.5M48%2080.9999C66.2254%2080.9999%2081%2066.2253%2081%2047.9999C81%2029.7745%2066.2254%2014.9999%2048%2014.9999C29.7746%2014.9999%2015%2029.7745%2015%2047.9999C15%2066.2253%2029.7746%2080.9999%2048%2080.9999Z'%20stroke='%23FF435A'%20stroke-width='2'%20stroke-linecap='round'%20stroke-linejoin='round'/%3e%3c/svg%3e";

const homeIcon1 =
  "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath d='m3 10 9-7 9 7v10a1 1 0 0 1-1 1h-5v-7H9v7H4a1 1 0 0 1-1-1z' fill='none' stroke='currentColor' stroke-linecap='round' stroke-linejoin='round' stroke-width='2'/%3E%3C/svg%3E";

const homeIcon =
  "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath d='m12 5.69 5 4.5V18h-2v-6H9v6H7v-7.81zM12 3 2 12h3v8h6v-6h2v6h6v-8h3z' /%3E%3C/svg%3E";

const ErrorFallback = () => {
  return (
    <main
      style={{
        minHeight: "100vh",
        boxSizing: "border-box",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        padding: "24px",
        backgroundColor: "#f7f8fa",
        fontFamily: "Arial, sans-serif",
      }}
    >
      <section
        role="alert"
        style={{
          boxSizing: "border-box",
          width: "100%",
          maxWidth: "560px",
          padding: "40px 32px",
          border: "1px solid #e3e6ea",
          borderRadius: "32px",
          backgroundColor: "#fff",
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          textAlign: "center",
        }}
      >
        <img
          src={errorIcon}
          alt=""
          aria-hidden="true"
          style={{ width: "96px", height: "96px", marginBottom: "24px" }}
        />
        <h1
          style={{
            margin: "0 0 12px",
            color: "#b42318",
            fontSize: "24px",
            lineHeight: 1.3,
            fontWeight: 700,
          }}
        >
          Грешка
        </h1>
        <p
          style={{
            margin: 0,
            // color: "#4b5563",
            color: "#b42318",
            fontSize: "16px",
            lineHeight: 1.6,
          }}
        >
          Опа, нещо се обърка.
        </p>
        <a
          href="/"
          style={{
            minHeight: "48px",
            boxSizing: "border-box",
            display: "inline-flex",
            alignItems: "center",
            justifyContent: "center",
            gap: "10px",
            marginTop: "32px",
            padding: "0 24px",
            borderRadius: "999px",
            backgroundColor: "#b42318",
            color: "#fff",
            fontSize: "16px",
            fontWeight: 700,
            textDecoration: "none",
          }}
        >
          <img
            src={homeIcon}
            alt=""
            aria-hidden="true"
            style={{ width: "20px", height: "20px", filter: "brightness(0) invert(1)" }}
          />
          Начало
        </a>
      </section>
    </main>
  );
};

export default ErrorFallback;
