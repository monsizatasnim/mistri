import React from 'react';
import { useT } from '../i18n/languageContext.js';

const CustomerProfilePage = () => {
    const { t } = useT();
    return (
        <div className="profile-wrap">
            <h2 className="profile-title">{t('Customer Profile & Feedback')}</h2>

            <section className="panel">
                <h3>{t('Profile Settings')}</h3>
                <label className="panel-label">{t('Profile Picture:')}</label>
                <input type="file" />
            </section>

            <section className="panel">
                <h3>{t('Service & Payment History')}</h3>
                <p className="pay-ok">{'\u2714'} {t('Last Payment')}: {'\u09F3'}500 ({t('Successful')})</p>
            </section>

            <section className="panel">
                <h3>{t('Submit Review')}</h3>
                <label className="panel-label">{t('Rating (1-5):')}</label>
                <input className="panel-input" type="number" min="1" max="5" defaultValue="5" />
                <textarea className="panel-area" placeholder={t('Write your review...')}></textarea>
                <button className="btn panel-btn">{t('Submit Review')}</button>
            </section>

            <section className="panel">
                <h3>{t('Lodge Complaint')}</h3>
                <textarea className="panel-area" placeholder={t('Describe your issue or complaint...')}></textarea>
                <button className="btn-small-red panel-btn">{t('Submit Complaint')}</button>
            </section>
        </div>
    );
};

export default CustomerProfilePage;
